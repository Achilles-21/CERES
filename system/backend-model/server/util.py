
from pydantic import BaseModel, Field
from typing import List, Dict
from pathlib import Path
import os, json
from langchain_openai import ChatOpenAI
import torch
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.caches import InMemoryCache
from langchain_core.globals import set_llm_cache
from langchain_core.messages import trim_messages, BaseMessage, HumanMessage, AIMessage, SystemMessage
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_core.runnables import ConfigurableFieldSpec
from datetime import datetime
import re
import shutil
from server.config import data_directory
from model_config import generation_config
from server.constants import system_messages, topic_extraction_prompt
import numpy as np
from lavis.models import load_model_and_preprocess
from PIL import Image
import io
import base64
import random
from openai import OpenAI

current_dir = Path(__file__).resolve().parent
data_dir = data_directory()


class blip2:

    def __init__(self):

        self.max_candidates = 10
        self.device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
        self.data_path = data_dir/"BLIP2"
        texts_embedding = np.load(self.data_path/"text_embeddings1.npy")
        self.texts_embedding = torch.tensor(texts_embedding).to(self.device)
        self.model, self.vis_processor, self.txt_processor = load_model_and_preprocess(
                                                                                        name="blip2_feature_extractor",
                                                                                        model_type="pretrain",
                                                                                        is_eval=True,
                                                                                        device=self.device
                                                                                      )
        with open(self.data_path/"merged_caption.json", 'r', encoding='utf-8') as f:
            self.image_map = json.load(f)
        self.texts = []
        self.names = []
        self.paths = []
        # Sort directories and images consistently so retrieval names align with embedding rows.
        all_entries = os.listdir(self.data_path)
        dirs = [entry for entry in all_entries if os.path.isdir(os.path.join(self.data_path, entry))]
        dirs = sorted(dirs, key=str.lower)
        for dir in dirs:
            name_images = os.listdir(self.data_path/dir)
            name_images = sorted(name_images)
            for name_image in name_images:
                self.texts.append(self.image_map[dir+"/"+name_image])
                self.names.append(dir+"/"+name_image)
                self.paths.append(self.data_path/dir/name_image)
        self.data_size = len(self.names)
        self.cnts = {}

    def find_images(self, text_target: str, num_retrieval: int) -> Dict:
        retrieval_map = []
        image_input = torch.rand(1, 3, 224, 224).to(self.device)
        text_input = self.txt_processor["eval"](text_target)
        sample = {"image": image_input, "text_input": [text_input]}
        text_emb = self.model.extract_features(sample, mode="text").text_embeds[0,0,:]
        text_emb /= text_emb.norm(dim=-1, keepdim=True)
        text_emb = text_emb.cpu().detach().numpy()
        similarity_vector = self.texts_embedding.cpu().detach().numpy() @ text_emb.T
        index_sorted = np.argsort(similarity_vector)
        for i in range(1, num_retrieval+1):
            idx = index_sorted[self.data_size-i]
            retrieval_map.append((self.names[idx], self.paths[idx], self.texts[idx], similarity_vector[idx]))
        return retrieval_map

    def naive_img_retrieve(self, kwd: str, top_k: int) -> tuple:
        probs = []
        items = self.find_images(kwd, top_k)
        for i in range(top_k):
            if items[i][0] not in self.cnts:
                self.cnts[items[i][0]] = 1
                return (items[i][2], img_encode(items[i][1]))
            else:
                probs.append(1 / self.cnts[items[i][0]])
        sum_probs = sum(probs)
        probs_norm = [x / sum_probs for x in probs]
        idxs = [i for i in range(top_k)]
        idx = random.choices(idxs, probs_norm, k=1)[0]
        img_name, img_path, txt, _ = items[idx]
        self.cnts[img_name] += 1
        return (txt, img_encode(img_path))

def save_to_dir(save_path: Path, retrieval_map: Dict) -> None:
    if not os.path.exists(save_path):
        os.makedirs(save_path)
    for i in range(1, len(retrieval_map)+1):
        img_name, img_path, _, _ = retrieval_map[i]
        img_save_path = save_path / img_name
        shutil.copy(img_path, img_save_path)

def image_to_base64(image: Image.Image) -> str:
    buffered = io.BytesIO()
    image.save(buffered, format="JPEG")
    img_bytes = buffered.getvalue()
    img_base64 = base64.b64encode(img_bytes)
    img_base64_str = img_base64.decode("utf-8")
    return img_base64_str

def img_encode(img_path: str) -> str:
    image = Image.open(img_path)
    return image_to_base64(image)

class InMemoryHistory(BaseChatMessageHistory, BaseModel):
    messages: List[BaseMessage] = Field(default_factory=list)

    def add_messages(self, messages: List[BaseMessage]) -> None:
        self.messages.extend(messages)

    def clear(self) -> None:
        self.messages = []

class HistoryManager:

    def __init__(self):
        self.__max_len = 400
        self.__current_file_path = Path(__file__).resolve().parent
        self.__chat_history_path = self.__current_file_path / "chat_history"
        if not os.path.exists(self.__chat_history_path):
            os.makedirs(self.__chat_history_path)
        all_items = os.listdir(self.__chat_history_path)
        if ".ipynb_checkpoints" in all_items:
            all_items.remove(".ipynb_checkpoints")
        self.__usrs = [item for item in all_items if os.path.isdir(os.path.join(self.__chat_history_path, item))]
        self.__conv_types = [0, 1, 2]

    def __get_file(self, usr: str, conv_type: int) -> Path:
        usr_path = self.__chat_history_path / usr
        if not os.path.exists(usr_path):
            os.makedirs(usr_path)
            for t in self.__conv_types:
                self.__create_new_file(str(usr_path / f"{str(t)}_0.json"))
            return usr_path / (str(conv_type) + "_0.json")
        fs = [f for f in os.listdir(usr_path) if f.startswith(str(conv_type))]
        f_idxs = [int(f.split('.')[0].split('_')[-1]) for f in fs]
        return usr_path / (str(conv_type) + "_" + str(max(f_idxs)) + ".json")

    @staticmethod
    def __create_new_file(f_path: str) -> None:
        with open(f_path, "w") as f:
            json.dump([], f)

    def save_message(self, usr: str, conv_type: int, msg_type: str, content: str, time: str) -> None:
        f_p = self.__get_file(usr, conv_type)
        with open(f_p, "r") as f:
            msgs = json.load(f)
        if len(msgs) >= self.__max_len:
            f_n = int(f_p.stem.split('_')[-1]) + 1
            f_p = self.__chat_history_path / usr / (str(conv_type) + "_" + str(f_n) + ".json")
            self.__create_new_file(str(f_p))
            msgs = []
        item = {
                    "time": time,
                    "msg_type": msg_type,
                    "content": content
               }
        msgs.append(item)
        with open(f_p, "w") as f:
            json.dump(msgs, f, indent=4)

    def load_messages(self, usr: str, conv_type: int) -> List[BaseMessage]:
        f_p = self.__get_file(usr, conv_type)
        with open(f_p, "r") as f:
            msgs = json.load(f)
        res = []
        for msg in msgs:
            if msg["msg_type"] == "ai":
                res.append(AIMessage(content=msg["content"]))
            elif msg["msg_type"] == "human":
                res.append(HumanMessage(content=msg["content"]))
        return res

    def load_history(self) -> Dict:
        history = {}
        for usr in self.__usrs:
            for t in self.__conv_types:
                history[(usr, t)] = InMemoryHistory()
                history[(usr, t)].add_messages([SystemMessage(content=system_messages[t])])
        return history

    def make_sure_usr(self, usr: str) -> bool:
        if not usr in self.__usrs:
            usr_path = self.__chat_history_path / usr
            os.makedirs(usr_path)
            for t in self.__conv_types:
                self.__create_new_file(str(usr_path / f"{str(t)}_0.json"))
            self.__usrs.append(usr)
            return False
        else:
            return True

class ModelWorker:
    def __init__(self):
        self.model = ChatOpenAI(
                                    **generation_config(),
                                    temperature=0.0,
                                    max_tokens=4096,
                                    timeout=None,
                                    max_retries=2
                                )
        set_llm_cache(InMemoryCache())
        self.history_manager = HistoryManager()
        self.user_chat_history = self.history_manager.load_history()
        self.chat_bot = RunnableWithMessageHistory(
                                                        runnable=self.model,
                                                        get_session_history=self.__get_user_history,
                                                        history_factory_config=[
                                                            ConfigurableFieldSpec(
                                                                id="user_id",
                                                                annotation=str,
                                                                name="User ID",
                                                                description="Unique identifier for the user.",
                                                                default="",
                                                                is_shared=True,
                                                            ),
                                                            ConfigurableFieldSpec(
                                                                id="conv_type",
                                                                annotation=int,
                                                                name="Conversation ID",
                                                                description="Unique identifier for the conversation.",
                                                                default="",
                                                                is_shared=True,
                                                            ),
                                                        ],
                                                     )


    def __get_user_history(self, user_id: str, conv_type: int) -> InMemoryHistory:
        return self.user_chat_history[(user_id, conv_type)]

    def __format_sys_msg(self, usr: str, conv_type: int, msg: str):
        c = re.findall('<c>(.*?)</c>', msg)
        q = re.findall('<q>(.*?)</q>', msg)
        d = re.findall('<d>(.*?)</d>', msg)
        # Rebuild the system prompt before applying the current case context.
        self.user_chat_history[(usr, conv_type)].messages[0].content = system_messages[conv_type]
        if len(q) == 0:
            self.user_chat_history[(usr, conv_type)].messages[0].content = self.user_chat_history[(usr, conv_type)].messages[0].content.format(c[0])
        else:
            self.user_chat_history[(usr, conv_type)].messages[0].content = self.user_chat_history[(usr, conv_type)].messages[0].content.format(c[0], q[0], d[0])

    def __invoke(self, usr_id: str, conv_type: int, human_msg: HumanMessage) -> str:
        config = {"configurable": {"user_id": usr_id, "conv_type": conv_type}}
        response = self.chat_bot.invoke(human_msg, config)
        return response.content

    def clear_history(self, usr_id: str, conv_type: int):
        if (usr_id, conv_type) in self.user_chat_history:
            self.user_chat_history[(usr_id, conv_type)].clear()
            # Restore the system message after clearing the conversation.
            self.user_chat_history[(usr_id, conv_type)].add_messages([SystemMessage(content=system_messages[conv_type])])

    def invoke(self, usr_id: str, conv_type: int, msg: str) -> str:
        if not self.history_manager.make_sure_usr(usr_id):
            self.user_chat_history = self.history_manager.load_history()
        if conv_type == 0:
            human_msg = HumanMessage(content=msg)
        else:
            self.__format_sys_msg(usr=usr_id, conv_type=conv_type, msg=msg)
            human_msg = HumanMessage(content=(re.findall('<s>(.*?)</s>', msg)[0]))
        response = self.__invoke(usr_id, conv_type, human_msg)
        # Persist only the newest request/response pair after each successful call.
        if response:
            last_request = self.user_chat_history[(usr_id, conv_type)].messages[-2]
            self.history_manager.save_message(usr_id, conv_type, "human", last_request.content, str(datetime.now()))
            last_response = self.user_chat_history[(usr_id, conv_type)].messages[-1]
            self.history_manager.save_message(usr_id, conv_type, "ai", last_response.content, str(datetime.now()))
        return response

if __name__ == '__main__':
    from config import set_environment
    set_environment()
    blip2 = blip2()
    text_target = "eveitis"
    text, img = blip2.retrieve_img_txt_pair(text_target)
    print(text)
    pass

_topic_cache = {}

def extract_topics(input_text: str, llm) -> dict:
    """
    Extract core ophthalmology concepts from student's question.
    Returns {"core_concepts": [...], "related_concepts": [...]}
    Uses in-memory cache to avoid redundant LLM calls.
    """
    cache_key = input_text.strip()[:100]
    if cache_key in _topic_cache:
        return _topic_cache[cache_key]

    try:
        prompt = topic_extraction_prompt.format(input_text)
        response = llm.invoke(prompt).content
        json_match = re.search(r'\{.*\}', response, re.DOTALL)
        if json_match:
            result = json.loads(json_match.group())
            if "core_concepts" in result and "related_concepts" in result:
                _topic_cache[cache_key] = result
                return result
    except Exception as e:
        pass

    default = {"core_concepts": [], "related_concepts": []}
    return default



_diverge_counter = {}

_understand_signals_zh = [
    "明白了", "懂了", "理解了", "我知道了", "清楚了", "了解了",
    "好的我明白", "不用再问了", "够了", "可以了", "掌握了", "学会了",
]

_understand_signals_en = [
    "i understand", "got it", "i see", "makes sense", "clear now",
    "no more questions", "that's enough", "i'm good", "understand now",
    "i got it", "makes total sense",
]


def should_summarize(user_id: str, input_text: str, max_rounds: int = 5) -> tuple:
    """
    Determine if the conversation should switch to summary mode.

    Increments the per-user divergence round counter and checks two conditions:
    1. Whether the student's reply contains an understanding signal.
    2. Whether the round count has reached the configured maximum.

    Returns:
        (should_summarize: bool, round_count: int, reason: str)
        reason is one of: "student_understood", "max_rounds_reached", "continue"
    """
    global _diverge_counter

    if user_id not in _diverge_counter:
        _diverge_counter[user_id] = 0
    _diverge_counter[user_id] += 1
    round_count = _diverge_counter[user_id]

    input_lower = input_text.lower().strip()
    for signal in _understand_signals_zh + _understand_signals_en:
        if signal in input_lower:
            return (True, round_count, "student_understood")

    if round_count >= max_rounds:
        return (True, round_count, "max_rounds_reached")

    return (False, round_count, "continue")


def reset_diverge_counter(user_id: str):
    """Reset the divergence counter for a user (called after summarization)."""
    global _diverge_counter
    if user_id in _diverge_counter:
        del _diverge_counter[user_id]
