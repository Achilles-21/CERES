# CERES

CERES is an online learning platform for ophthalmology that combines VLM training with an online virtual teacher. This repository contains two parts: `model/` for model training and personalized curriculum recommendation, and `system/` for the teaching application and its backend services.

## Project Structure

```text
CERES/
├── README.md
├── requirements.txt                 # model training dependencies
├── model/
│   ├── dqn/                         # curriculum environment, DQN training and inference
│   └── grpo/
│       ├── ceres_plugin/            # teaching environment, rewards and student simulator
│       ├── configs/                 # reward configuration
│       ├── data_pipeline/           # training data preparation
│       ├── scripts/                 # data preparation and training launchers
│       └── trainers/                # multi-turn rollout and action-level GRPO
└── system/
    ├── frontend/                    # teacher and student web application
    ├── backend-system/              # Java teaching business backend
    ├── backend-model/               # model inference and multi-agent slide generation
    │   └── slidegen/                # slide API, agents, PPT export and standalone UI
    └── scripts/                     # service launchers and verification tools
```

## Model Training

The training code fine-tunes Qwen2.5-VL-32B-Instruct with LoRA using SFT and multi-turn GRPO with [ms-swift 3.2.1](https://github.com/modelscope/ms-swift/releases/tag/v3.2.1). Both launchers default to 8 NVIDIA A800 GPUs (8 ranks, GPU IDs 0–7). A virtual student simulator produces teaching dialogues, and sequence rewards together with anchor credit guide the model toward structured teaching actions.

### Install Dependencies

From the repository root, install the training dependencies with:

```bash
pip install -r requirements.txt
```

The training stack pins `ms-swift==3.2.1`, `transformers==4.50.3`, `trl==0.16.1` and `peft==0.14.0` to satisfy this release's dependency bounds. CERES supplies its multi-turn teaching loop through repository-owned GRPO trainers because 3.2.1 has no `--multi_turn_func` option. Both action credit and the scalar ablation use synchronous PyTorch inference; vLLM and LMDeploy are disabled for these trainers.

### Prepare Data

Prepare training datasets separately using the [data preparation modules](model/grpo/data_pipeline/): normalize source datasets, rewrite learner queries, synthesize teaching dialogues and filter SFT trajectories. Set `CERES_SFT_DATASET` to the SFT dialogue JSONL and `CERES_DATASET` to the GRPO query JSONL before training.

### SFT Warmup and GRPO Training

Run the following commands from `model/grpo/`:

```bash
export CERES_MODEL=path/to/Qwen2.5-VL-32B-Instruct
export CUDA_VISIBLE_DEVICES=0,1,2,3,4,5,6,7
export NPROC_PER_NODE=8

export CERES_SFT_DATASET=path/to/dataset
bash scripts/run_sft_warmup.sh

export CERES_STUDENT_API_KEY="<your-api-key>"
export CERES_ADAPTERS=output/ceres-oph-sft-warmup
export CERES_DATASET=path/to/dataset
bash scripts/run_grpo_oph_lora.sh
```

Replace the model and dataset placeholders with actual paths. The two dataset variables must point to the appropriate SFT dialogue JSONL and GRPO query JSONL, respectively. Relative paths resolve from `model/grpo/`. Both stages use LoRA rank 64, alpha 128 and dropout 0.05; the default effective batch is 1 × 8 × 16 = 128.

SFT warmup and GRPO both read `CERES_MODEL` and default to Qwen2.5-VL-32B-Instruct. Use the same base model when loading the resulting LoRA adapter in the deployment service.

In ms-swift 3.2.1, each GRPO generation group must fit in the global microbatch (`per_device_train_batch_size × world_size`), independently of gradient accumulation. With the defaults, 8 completions for one prompt span 8 ranks; accumulating 16 microbatches forms one optimizer update. The launcher checks this divisibility before training. `CERES_CREDIT_MODE=action` selects action-level credit by default; `scalar` selects the scalar ablation.

## Online Teaching System

Teaching dialogue, course generation, MedRAG and slide generation use the trained CERES model identifier `path/to/ceres/model` through an OpenAI-compatible inference service. Configure `CERES_BACKEND_MODEL` and `CERES_LLM_BASE_URL` in `system/backend-model/.env`; the model path must match the identifier exposed by the service. See the [backend configuration](system/backend-model/README.md).

The frontend is built with **Vue 3**, **Vite** and **TypeScript**, using **Naive UI** for interface components, **Pinia** for state management, **Vue Router** for navigation and **Tailwind CSS** for styling. The teaching business backend uses **Java 11**, **Spring Boot**, **Spring Security** and **MyBatis-Plus**, organized as a Maven multi-module project. Python **FastAPI** services provide online model inference and multi-agent slide generation, with an additional Vue interface for standalone slide authoring. Besides the application code, deployment requires **MySQL** to store structured data such as users, courses, questions and learning records; **Redis** to manage caches, authentication-related state and temporary data; and **MinIO** to store unstructured resources such as images, audio, video, documents and generated PPT files.

### System Components

| Component | Responsibilities |
| --- | --- |
| `frontend/` | Teacher and student interfaces for courses, teaching dialogue, exercises, exams, notes, forums and slide generation. |
| `backend-system/` | Business APIs for account permissions, course resources, question banks, assessments and learning records. |
| `backend-model/` | Teaching dialogue, exercise and case recommendations, image retrieval, speech recognition and synthesis, optional RAG/MedRAG, and multi-agent slide planning, writing, review, revision and PPT export through `slidegen/`. |

### Java Backend Modules

The Java backend separates application entry points, business logic, shared utilities and security infrastructure:

| Maven module | Responsibilities |
| --- | --- |
| `ceres-admin` | Spring Boot application entry point and HTTP controllers exposing teaching and administration APIs. |
| `ceres-system` | Business services, domain models and MyBatis mappings for teaching workflows and system management. |
| `ceres-common` | Shared utilities, common data structures, configuration, file handling, MinIO storage and cloud video helpers. |
| `ceres-framework` | Authentication, authorization, token handling, security filters and supporting web infrastructure. |

Important functions include:

- **User and permission management:** user accounts, roles, menus, departments and access control for teaching and administration.
- **Course and resource management:** courses, teaching media, uploaded resources and access to MinIO storage.
- **Question banks and assessments:** question management, exercises, examinations and answer records.
- **Virtual teacher integration:** connections to the model service for teaching dialogue, case-based practice, speech and learning summaries.
- **Case-based practice:** clinical case access and simulated patient/doctor interactions through the model service.
- **Learning records and reports:** course participation, exercise results, learning duration and generated learning summaries.
- **Teaching collaboration and materials:** notes, forum discussions and replies, resource collections and PPT material management.

### Dependencies and Setup

Install the shared Python backend dependencies from the repository root:

```bash
pip install -r system/backend-model/requirements.txt
```

The Vue applications use their own Node.js dependency manifests, and the Java backend uses Maven. Install and configure MySQL, Redis and MinIO separately, then fill in the appropriate `.env.example` files with local service addresses and credentials. Model weights, authorized datasets and database initialization data must also be prepared separately.

Model inference and slide generation run as separate API processes inside `backend-model/`, sharing its configuration and dependency entry point. See [`system/backend-model/README.md`](system/backend-model/README.md) for configuration and startup commands.
