# -*- coding: utf-8 -*-
'Best-effort adapter saving on uncaught exceptions, SIGTERM and SIGINT.'


import logging
import os
import signal
import sys
import threading

__all__ = ["SaveOnExit", "register_save_on_exit", "emergency_save", "install_exit_hooks",
           "uninstall_exit_hooks", "EMERGENCY_DIRNAME", "TRANSFORMERS_AVAILABLE"]

_LOG = logging.getLogger(__name__)


EMERGENCY_DIRNAME = "emergency-adapter"


try:
    from transformers.trainer_callback import TrainerCallback as _TrainerCallback
    TRANSFORMERS_AVAILABLE = True
except Exception:  # noqa: BLE001
    class _TrainerCallback:  # type: ignore[no-redef]
        pass

    TRANSFORMERS_AVAILABLE = False


_STATE = {
    "model": None,
    "output_dir": None,
    "saved": False,
    "save_path": None,
    "hooks_installed": False,
    "orig_excepthook": None,
    "orig_handlers": {},
}
_LOCK = threading.RLock()

_HANDLED_SIGNALS = (signal.SIGTERM, signal.SIGINT)


def _local_rank():
    try:
        return int(str(os.environ.get("LOCAL_RANK", "0") or "0").strip())
    except (TypeError, ValueError):
        return 0


def _output_dir_of(args):
    try:
        v = getattr(args, "output_dir", None)
        return str(v).strip() if v else None
    except Exception:
        return None


def _capture_reference(model, output_dir):
    with _LOCK:
        if model is not None:
            _STATE["model"] = model
        if output_dir:
            _STATE["output_dir"] = output_dir


def emergency_save(trigger=""):
    'Save once on local rank zero; saving failures must not mask the original error.'


    with _LOCK:
        if _STATE["saved"]:
            return False
        _STATE["saved"] = True
        model = _STATE.get("model")
        output_dir = _STATE.get("output_dir")
    if model is None or not output_dir:
        _LOG.warning("[save_on_exit] 触发（%s）但 model/output_dir 尚未捕获，跳过保存", trigger)
        return False
    rank = _local_rank()
    if rank != 0:
        _LOG.info("[save_on_exit] 触发（%s）但 local_rank=%d ≠ 0，跳过（rank0 负责保存）",
                  trigger, rank)
        return False
    path = os.path.join(output_dir, EMERGENCY_DIRNAME)
    _LOG.warning("=" * 72)
    _LOG.warning("[save_on_exit] ⚠ 进程即将退出（触发：%s）——正在紧急保存 LoRA 增量到 %s",
                 trigger or "unknown", path)
    _LOG.warning("=" * 72)
    try:
        model.save_pretrained(path)
        with _LOCK:
            _STATE["save_path"] = path
        _LOG.warning("[save_on_exit] ✔ 紧急保存完成：%s（可用 --adapters %s 挂载续训）", path, path)
        return True
    except Exception as e:                     # noqa: BLE001
        _LOG.error("[save_on_exit] ✘ 紧急保存失败（%s）：%s——放弃保存，原异常/退出语义不变",
                   type(e).__name__, e)
        return False


def _excepthook(exc_type, exc_value, exc_tb):
    try:
        emergency_save("sys.excepthook:%s" % getattr(exc_type, "__name__", exc_type))
    finally:
        hook = _STATE.get("orig_excepthook") or sys.__excepthook__
        try:
            sys.excepthook = hook
        except Exception:                      # noqa: BLE001
            pass
        hook(exc_type, exc_value, exc_tb)


def _signal_handler(signum, frame):  # noqa: ARG001

    try:
        emergency_save("signal:%d" % int(signum))
    finally:
        try:
            signal.signal(signum, signal.SIG_DFL)
        except Exception:                      # noqa: BLE001
            pass
        os.kill(os.getpid(), signum)


def install_exit_hooks():
    with _LOCK:
        if _STATE["hooks_installed"]:
            return True
        _STATE["hooks_installed"] = True
        _STATE["orig_excepthook"] = sys.excepthook
        for sig in _HANDLED_SIGNALS:
            try:
                _STATE["orig_handlers"][int(sig)] = signal.getsignal(sig)
            except Exception:                  # noqa: BLE001
                pass
    sys.excepthook = _excepthook
    ok = True
    for sig in _HANDLED_SIGNALS:
        try:
            signal.signal(sig, _signal_handler)
        except Exception as e:                 # noqa: BLE001
            ok = False
            _LOG.warning("[save_on_exit] 无法安装信号处理器 sig=%s（%s）", sig, e)
    _LOG.info("[save_on_exit] 崩溃保护已就绪（sys.excepthook + %s）——异常/kill 退出前将保存 "
              "LoRA 到 <output_dir>/%s", "/".join(str(int(s)) for s in _HANDLED_SIGNALS),
              EMERGENCY_DIRNAME)
    return ok


def uninstall_exit_hooks():
    with _LOCK:
        if not _STATE["hooks_installed"]:
            return
        _STATE["hooks_installed"] = False
        orig_hook = _STATE.get("orig_excepthook")
        handlers = dict(_STATE.get("orig_handlers") or {})
        _STATE["orig_excepthook"] = None
        _STATE["orig_handlers"] = {}
    try:
        if orig_hook is not None:
            sys.excepthook = orig_hook
    except Exception:                          # noqa: BLE001
        pass
    for sig, handler in handlers.items():
        try:
            signal.signal(sig, handler if handler is not None else signal.SIG_DFL)
        except Exception:                      # noqa: BLE001
            pass


class SaveOnExit(_TrainerCallback):
    def on_train_begin(self, args=None, state=None, control=None, **kwargs):
        _capture_reference(kwargs.get("model"), _output_dir_of(args))
        install_exit_hooks()

    def on_step_end(self, args=None, state=None, control=None, model=None, **kwargs):
        _capture_reference(model if model is not None else kwargs.get("model"),
                           _output_dir_of(args))
        install_exit_hooks()

    def on_train_end(self, args=None, state=None, control=None, **kwargs):
        'Keep exit hooks active for failures during final evaluation and flushing.'
        _capture_reference(kwargs.get("model"), _output_dir_of(args))

    def on_exception(self, args=None, state=None, control=None, exception=None, **kwargs):
        trigger = ("on_exception:%s" % type(exception).__name__
                   if exception is not None else "on_exception")
        return emergency_save(trigger)


def register_save_on_exit(callbacks_list=None):
    'Append the callback to the shared Swift registry before trainer construction.'


    targets = []
    if callbacks_list is not None:
        targets.append(callbacks_list)
    else:
        try:
            from swift.plugin import extra_callbacks as _swift_extra_callbacks
            targets.append(_swift_extra_callbacks)
        except Exception as e:                 # noqa: BLE001
            _LOG.warning("[save_on_exit] swift 不可用（%s）——无法注册进 extra_callbacks，"
                         "本次进程无崩溃保护", type(e).__name__)
            return False
    cb = SaveOnExit()
    for lst in targets:
        if not any(isinstance(c, SaveOnExit) for c in lst):
            lst.append(cb)
    _LOG.info("[save_on_exit] SaveOnExit 已注册（%d 处回调表）——异常/kill 退出前将保存 LoRA "
              "增量到 <output_dir>/%s", len(targets), EMERGENCY_DIRNAME)
    return True
