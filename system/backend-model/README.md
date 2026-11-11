# CERES Python Backend

The Python backend contains model inference and multi-agent slide authoring. Both services use `backend-model/.env` and the root `requirements.txt` in this directory. They run as separate FastAPI processes so slide authoring can start without loading the local BLIP2 model or inference datasets.

## Modules

| Directory | Function |
| --- | --- |
| `server/` | Teaching dialogue, exercises and cases, image retrieval, speech recognition and synthesis. |
| `slidegen/api/` | Slide HTTP APIs and WebSocket sessions for agent messages and teacher feedback. |
| `slidegen/main_web.py` | Designer, planner, writer and reviewer agent coordination, revision and image requests. |
| `slidegen/ppt_generator.py` | Xunfei AIPPT template selection and PPT export. |
| `slidegen/frontend/` | Optional standalone Vue 3 slide authoring UI; the main teaching UI also supports slides. |
| `slidegen/ProjFile/Reference/` | Retained vendor reference scripts and architecture resource. |
| `module_RAG/` | RAG preprocessing and optional MedRAG service. |
| `Client/` | Manual model API integration demos. |

## Install and configure

From `system/backend-model/`, using Python 3.10:

```sh
python -m pip install -r requirements.txt
cp .env.example .env
```

Supply `OPENAI_API_KEY` and, when needed, `OPENAI_BASE_URL`. PPT export additionally requires `XUNFEI_APP_ID` and `XUNFEI_API_SECRET`; speech recognition uses the separate `XUNFEI_ASR_*` credentials. Keep credentials in your environment or untracked `.env`; browser `VITE_*` variables must contain only public settings.

Teaching dialogue, course generation, MedRAG, slide agents, revisions, feedback classification and slide image-request generation share the CERES generation model:

```sh
CERES_BACKEND_MODEL=path/to/ceres/model
CERES_LLM_BASE_URL=http://127.0.0.1:8002/v1
```

Serve the trained Qwen2.5-VL-32B CERES weights (merged weights or a loaded LoRA adapter) through an OpenAI-compatible inference server, and replace these placeholders with its model identifier and address. The model field is passed to that server; these API clients do not load weights from the filesystem. `CERES_LLM_BASE_URL` takes precedence over `OPENAI_BASE_URL` and `OPENAI_API_BASE`; without any configured address, generation defaults to `http://127.0.0.1:8002/v1`. Set `OPENAI_API_KEY` to the inference service's credential (or an accepted placeholder for an unauthenticated local service). Embedding and speech clients retain their separate OpenAI configuration. AutoGen receives explicit custom-model capabilities, so the slash-containing model identifier does not require a known GPT model name; slide JSON remains prompt-based, with no tool calling or enforced structured-output API assumed.

The custom client configuration follows the [AutoGen model client documentation](https://microsoft.github.io/autogen/stable/user-guide/agentchat-user-guide/tutorial/models.html).

User-facing text from `server/server.py` is reviewed by an additional call to the existing CERES model before it is returned. The review sends only outgoing answer text, without request context, identifiers, images or reference documents, and uses the same model endpoint and credentials. Home replies, teaching dialogue, case simulation, exercise explanations, generated course answers and summaries are covered; internal classification and retrieval calls are unchanged. Only a valid JSON decision with `approved: true` releases the original response. A rejection, invalid decision, model error or 20-second timeout returns a fixed notice in the existing answer field and adds `moderation.status` (`blocked` or `unavailable`). Withheld replies also clear attached images, documents, concepts and topics; home navigation is disabled. No frontend change or additional model configuration is required.

For slide authoring alone, use `python -m pip install -r slidegen/requirements.txt`. The full requirements also contain GPU/model dependencies; select a compatible PyTorch/CUDA and BLIP2/LAVIS installation. The optional MedRAG service retains its own requirements. Local inference needs external authorized datasets and weights; see the [system setup](../README.md).

## Start

From `system/`, activate the backend Python environment and run each service in a separate terminal:

```sh
bash scripts/start.sh model          # server.server:app, port 8000
bash scripts/start.sh slidegen       # slidegen.api.server:app, port 8081
```

The launcher reads `system/.env`, then `backend-model/.env`. `CERES_HOST`, `CERES_MODEL_PORT` and `CERES_SLIDEGEN_PORT` configure listening addresses. For a direct launch from `backend-model/`:

```sh
python -m uvicorn slidegen.api.server:app --host 127.0.0.1 --port 8081
```

The slide package also reads `backend-model/.env` for direct Python entry points without overriding injected environment values. Use `python -m slidegen.main <user_id>` for the interactive CLI and `python -m slidegen.test_vapi` for the manual API demo. Both require the configured CERES inference endpoint; they are not offline tests. Export the same generation settings when launching the optional MedRAG service from its own directory.

The main UI keeps `/slidegen-api` and `/slidegen-ws` proxy routes to port 8081. The standalone UI uses `/api` and `/ws` and can be started from `system/` with `bash scripts/start.sh slide-frontend`, after `npm ci` in `backend-model/slidegen/frontend/`. Production reverse proxies must support WebSocket upgrades.

Image retrieval calls `CERES_MODEL_URL` (default `http://localhost:8000`). Run local inference or provide a compatible remote CERES model API when images are needed.

## Runtime data and checks

Slide drafts, image metadata, images and generated PPTs share one cache directory across agents, HTTP downloads and the PPT exporter. `CERES_SLIDE_CACHE_DIR` defaults to `backend-model/SlideCache/`. Relative configured paths resolve from `backend-model/`, independent of the working directory. Keep generated content outside Git; the default cache is ignored. If reusing an existing local deployment cache, set this variable to its directory. No source user cache was copied.

Offline checks from `backend-model/`:

```sh
python -m pip install pytest httpx
python -m pytest slidegen/test_localization.py slidegen/tests -q
```

These checks use temporary synthetic slide files and make no external API calls. The full slide pipeline, image retrieval and Xunfei export require operator-provided services and credentials.
