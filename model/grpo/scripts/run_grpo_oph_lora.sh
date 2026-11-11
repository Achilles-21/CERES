#!/bin/bash
# Teacher rollouts use synchronous PyTorch inference; CERES_MAX_TURNS caps dialogue length.
set -euo pipefail
cd "$(dirname "$0")/.."

export CERES_STUDENT_API_BASE=${CERES_STUDENT_API_BASE:-https://dashscope.aliyuncs.com/compatible-mode/v1}
export CERES_STUDENT_MODEL=${CERES_STUDENT_MODEL:-qwen-plus}
export CERES_STUDENT_CONCURRENCY=${CERES_STUDENT_CONCURRENCY:-64}
export CERES_STUDENT_TIMEOUT=${CERES_STUDENT_TIMEOUT:-30}
if [ -z "${CERES_STUDENT_API_KEY:-}" ] && [ "${CERES_STUDENT_MOCK:-0}" != "1" ]; then
    echo "[ceres-grpo] 错误：未设置 CERES_STUDENT_API_KEY。" >&2
    echo "[ceres-grpo] 正式训练：export CERES_STUDENT_API_KEY=sk-xxx 后重跑；" >&2
    echo "[ceres-grpo] 离线自测：export CERES_STUDENT_MOCK=1（student_sim 走确定性 mock）。" >&2
    exit 1
fi
export CERES_MAX_TURNS=${CERES_MAX_TURNS:-5}
CERES_NUM_GENERATIONS=${CERES_NUM_GENERATIONS:-8}
export CERES_NUM_GENERATIONS
export CERES_TRAJ_DIR=${CERES_TRAJ_DIR:-./traj_dump}
export CERES_END_PENALTY=${CERES_END_PENALTY:-0.0}
export CERES_CREDIT_MODE=${CERES_CREDIT_MODE:-action}
export CERES_REWARD_CONFIG=${CERES_REWARD_CONFIG:-configs/rewards/default.json}
if [ "${CERES_USE_VLLM:-0}" = "1" ]; then
    echo "[ceres-grpo] ms-swift 3.2.1 的 CERES 多轮适配器只支持同步 pt infer，请移除 CERES_USE_VLLM=1。" >&2
    exit 1
fi
case "$CERES_CREDIT_MODE" in
    action)
        if [ -n "${CERES_REWARD_WEIGHTS:-}" ]; then
            echo "[ceres-grpo] action 模式用配置中的 lambda_seq 混合优势；请移除 CERES_REWARD_WEIGHTS。" >&2
            exit 1
        fi
        CREDIT_ARGS=(--external_plugins ceres_plugin/action_plugin.py --reward_funcs ceres_sequence --reward_weights 1.0)
        ;;
    scalar)
        read -r -a SCALAR_WEIGHTS <<< "${CERES_REWARD_WEIGHTS:-1.0 0.1}"
        CREDIT_ARGS=(--external_plugins ceres_plugin/scalar_plugin.py --reward_funcs ceres_sequence ceres_anchor --reward_weights "${SCALAR_WEIGHTS[@]}")
        ;;
    *) echo "CERES_CREDIT_MODE must be action or scalar" >&2; exit 1 ;;
esac
echo "[ceres-grpo] credit_mode=$CERES_CREDIT_MODE | reward_config=$CERES_REWARD_CONFIG"
export PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True

CERES_LR=${CERES_LR:-3e-6}
CERES_BETA=${CERES_BETA:-0.08}
CERES_WARMUP_RATIO=${CERES_WARMUP_RATIO:-0.1}

CERES_MODEL=${CERES_MODEL:-path/to/Qwen2.5-VL-32B-Instruct}
CERES_ADAPTERS=${CERES_ADAPTERS:-}
CERES_OUTPUT_DIR=${CERES_OUTPUT_DIR:-output/ceres-oph-grpo-lora-v1}
CERES_DATASET=${CERES_DATASET:-path/to/dataset}
NPROC_PER_NODE=${NPROC_PER_NODE:-8}
CUDA_VISIBLE_DEVICES=${CUDA_VISIBLE_DEVICES:-0,1,2,3,4,5,6,7}
echo "[ceres-grpo] model=${CERES_MODEL} | dataset=${CERES_DATASET} | ranks=${NPROC_PER_NODE} | GPUs=${CUDA_VISIBLE_DEVICES} | LoRA r=64 alpha=128 dropout=0.05"

PY_BIN=${PY_BIN:-$(command -v python3)}
"$PY_BIN" -c 'from importlib.metadata import version; actual = version("ms-swift"); assert actual == "3.2.1", f"CERES requires ms-swift==3.2.1, got {actual}"'
ENGINE_FLAGS=""
CERES_ENGINE="ddp"
if "$PY_BIN" -c "import deepspeed" 2>/dev/null; then
    ENGINE_FLAGS="$ENGINE_FLAGS --deepspeed ${CERES_DEEPSPEED:-zero3}"
    CERES_ENGINE="zero3"
fi
if "$PY_BIN" -c "import flash_attn" 2>/dev/null; then
    ENGINE_FLAGS="$ENGINE_FLAGS --attn_impl flash_attn"
fi
if [ "$CERES_ENGINE" = "zero3" ]; then
    CERES_MAX_LENGTH=${CERES_MAX_LENGTH:-8192}
    CERES_MAX_COMPLETION=${CERES_MAX_COMPLETION:-1024}
else
    CERES_MAX_LENGTH=${CERES_MAX_LENGTH:-1024}
    CERES_MAX_COMPLETION=${CERES_MAX_COMPLETION:-256}
    echo "[ceres-grpo] ⚠ 无 deepspeed → 纯 DDP + 收缩上下文（max_length=${CERES_MAX_LENGTH}，completion=${CERES_MAX_COMPLETION}）"
    echo "[ceres-grpo]   32B 多轮教学建议先：pip install deepspeed（启用 ZeRO-3 后自动恢复 8192 上下文）"
fi
echo "[ceres-grpo] 引擎：${CERES_ENGINE} | ENGINE_FLAGS='${ENGINE_FLAGS}' | max_length=${CERES_MAX_LENGTH}"
# Use reentrant checkpointing for ZeRO-3 and non-reentrant checkpointing for plain DDP.
if [ "$CERES_ENGINE" = "zero3" ]; then
    GC_KWARGS='{"use_reentrant": true}'
else
    GC_KWARGS='{"use_reentrant": false}'
fi
PER_DEV_BS=${PER_DEV_BS:-1}
GRAD_ACCUM=${GRAD_ACCUM:-16}

if [ ! -f "$CERES_DATASET" ]; then
    echo "[ceres-grpo] 错误：GRPO 查询集不存在：$CERES_DATASET" >&2
    echo "[ceres-grpo] 请先运行数据管线（data_pipeline/build_query_dataset.py 产出，" >&2
    echo "[ceres-grpo] 命令块见 data_pipeline/README.md 与 data/README.md），或用 CERES_DATASET 指向已有文件。" >&2
    exit 1
fi

# Generation groups must fit the global microbatch; accumulation does not affect grouping.
WORLD_SIZE=${WORLD_SIZE:-$((NPROC_PER_NODE * ${NNODES:-1}))}
GENERATION_BATCH=$((PER_DEV_BS * WORLD_SIZE))
if [ "$CERES_NUM_GENERATIONS" -lt 2 ] || [ $((GENERATION_BATCH % CERES_NUM_GENERATIONS)) -ne 0 ]; then
    echo "[ceres-grpo] 错误：generation_batch(${GENERATION_BATCH}) 必须被 num_generations(${CERES_NUM_GENERATIONS}) 整除。" >&2
    echo "[ceres-grpo] 当前：PER_DEV_BS=${PER_DEV_BS} × WORLD_SIZE=${WORLD_SIZE} = ${GENERATION_BATCH}；num_generations 至少为 2，梯度累积不参与分组。" >&2
    exit 1
fi
echo "[ceres-grpo] 批次校验通过：generation_batch=${GENERATION_BATCH}，${GENERATION_BATCH}/${CERES_NUM_GENERATIONS}=$((GENERATION_BATCH / CERES_NUM_GENERATIONS)) 组独立 prompt/step"

# Resume SFT adapter weights without restoring its optimizer or scheduler.
ADAPTER_ARGS=()
if [ -n "$CERES_ADAPTERS" ]; then
    ADAPTER_ARGS=(--resume_from_checkpoint "$CERES_ADAPTERS" --resume_only_model true)
    echo "[ceres-grpo] 挂载 SFT 预热 adapter（resume_only_model）：$CERES_ADAPTERS"
fi

MAX_STEPS_ARGS=()
if [ -n "${CERES_MAX_STEPS:-}" ]; then
    MAX_STEPS_ARGS=(--max_steps "$CERES_MAX_STEPS")
    echo "[ceres-grpo] 步数上限：$CERES_MAX_STEPS（验证段；吞吐达标后去掉重跑全量）"
fi

# Default loss scaling includes teacher turns and masks student turns.
CUDA_VISIBLE_DEVICES="$CUDA_VISIBLE_DEVICES" \
NPROC_PER_NODE="$NPROC_PER_NODE" \
swift rlhf \
    --rlhf_type grpo \
    --model "$CERES_MODEL" \
    "${ADAPTER_ARGS[@]+"${ADAPTER_ARGS[@]}"}" \
    --train_type lora \
    --lora_rank 64 --lora_alpha 128 --lora_dropout 0.05 \
    --freeze_vit true --freeze_aligner true \
    --torch_dtype bfloat16 \
    --dataset "$CERES_DATASET" \
    --max_length "$CERES_MAX_LENGTH" \
    "${CREDIT_ARGS[@]}" \
    --loss_scale default \
    --num_generations "$CERES_NUM_GENERATIONS" \
    --num_iterations 1 \
    --max_completion_length "$CERES_MAX_COMPLETION" \
    --temperature 1.0 --top_p 0.99 \
    --beta "$CERES_BETA" --epsilon 0.2 \
    --per_device_train_batch_size "$PER_DEV_BS" \
    --gradient_accumulation_steps "$GRAD_ACCUM" \
    --learning_rate "$CERES_LR" \
    --warmup_ratio "$CERES_WARMUP_RATIO" \
    --num_train_epochs 2 \
    "${MAX_STEPS_ARGS[@]+"${MAX_STEPS_ARGS[@]}"}" \
    $ENGINE_FLAGS \
    --gradient_checkpointing true \
    --gradient_checkpointing_kwargs "$GC_KWARGS" \
    --split_dataset_ratio 0 \
    --log_completions true \
    --logging_steps 5 \
    --report_to tensorboard \
    --save_steps 50 --save_total_limit 5 \
    --add_version false \
    --output_dir "$CERES_OUTPUT_DIR" 2>&1 | tee train_oph_lora.log

echo "[ceres-grpo] 训练结束，输出目录：$CERES_OUTPUT_DIR"
if [ "${CERES_USE_VLLM:-0}" != "1" ]; then
    echo "[ceres-grpo] 本次为 ms-swift 同步 pt infer 多轮教学路径。"
fi
