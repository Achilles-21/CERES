#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/.."

CERES_MODEL=${CERES_MODEL:-path/to/Qwen2.5-VL-32B-Instruct}
CERES_OUTPUT_DIR=${CERES_OUTPUT_DIR:-output/ceres-oph-sft-warmup}
CERES_SFT_DATASET=${CERES_SFT_DATASET:-path/to/dataset}
CERES_LR=${CERES_LR:-1e-5}
CERES_EPOCHS=${CERES_EPOCHS:-1}
NPROC_PER_NODE=${NPROC_PER_NODE:-8}
CUDA_VISIBLE_DEVICES=${CUDA_VISIBLE_DEVICES:-0,1,2,3,4,5,6,7}
echo "[ceres-sft] model=${CERES_MODEL} | dataset=${CERES_SFT_DATASET} | ranks=${NPROC_PER_NODE} | GPUs=${CUDA_VISIBLE_DEVICES} | LoRA r=64 alpha=128 dropout=0.05"
export PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True

PY_BIN=${PY_BIN:-$(command -v python3)}
"$PY_BIN" -c 'from importlib.metadata import version; actual = version("ms-swift"); assert actual == "3.2.1", f"CERES requires ms-swift==3.2.1, got {actual}"'
ENGINE_FLAGS=""
CERES_ENGINE="ddp"
if "$PY_BIN" -c "import deepspeed" 2>/dev/null; then
    ENGINE_FLAGS="$ENGINE_FLAGS --deepspeed zero3"
    CERES_ENGINE="zero3"
fi
if "$PY_BIN" -c "import flash_attn" 2>/dev/null; then
    ENGINE_FLAGS="$ENGINE_FLAGS --attn_impl flash_attn"
fi
if [ "$CERES_ENGINE" = "zero3" ]; then
    CERES_MAX_LENGTH=${CERES_MAX_LENGTH:-4096}
else
    CERES_MAX_LENGTH=${CERES_MAX_LENGTH:-1024}
    echo "[ceres-sft] ⚠ 无 deepspeed → 纯 DDP + 收缩上下文（max_length=${CERES_MAX_LENGTH}）；32B 建议 pip install deepspeed"
fi
echo "[ceres-sft] 引擎：${CERES_ENGINE} | ENGINE_FLAGS='${ENGINE_FLAGS}' | max_length=${CERES_MAX_LENGTH}"
# Use reentrant checkpointing for ZeRO-3 and non-reentrant checkpointing for plain DDP.
if [ "$CERES_ENGINE" = "zero3" ]; then
    GC_KWARGS='{"use_reentrant": true}'
else
    GC_KWARGS='{"use_reentrant": false}'
fi

if [ ! -f "$CERES_SFT_DATASET" ]; then
    echo "[ceres-sft] 错误：SFT 数据集不存在：$CERES_SFT_DATASET" >&2
    echo "[ceres-sft] 请先运行数据管线生成（四步命令见 data_pipeline/README.md，" >&2
    echo "[ceres-sft] 最终产物由 data_pipeline/quality_filter.py 写出），或用 CERES_SFT_DATASET 指向已有文件。" >&2
    exit 1
fi

# Default loss scaling trains only assistant responses, excluding student turns.
CUDA_VISIBLE_DEVICES="$CUDA_VISIBLE_DEVICES" \
NPROC_PER_NODE="$NPROC_PER_NODE" \
swift sft \
    --model "$CERES_MODEL" \
    --train_type lora \
    --lora_rank 64 --lora_alpha 128 --lora_dropout 0.05 \
    --freeze_vit true --freeze_aligner true \
    --torch_dtype bfloat16 \
    --dataset "$CERES_SFT_DATASET" \
    --max_length "$CERES_MAX_LENGTH" \
    --split_dataset_ratio 0 \
    --loss_scale default \
    --per_device_train_batch_size 1 \
    --gradient_accumulation_steps 16 \
    --learning_rate "$CERES_LR" \
    --num_train_epochs "$CERES_EPOCHS" \
    --gradient_checkpointing true \
    --gradient_checkpointing_kwargs "$GC_KWARGS" \
    $ENGINE_FLAGS \
    --logging_steps 5 \
    --save_steps 50 --save_total_limit 2 \
    --report_to tensorboard \
    --add_version false \
    --output_dir "$CERES_OUTPUT_DIR" 2>&1 | tee sft_warmup.log

echo "[ceres-sft] SFT 预热完成。GRPO 衔接：CERES_ADAPTERS=$CERES_OUTPUT_DIR bash scripts/run_grpo_oph_lora.sh"
