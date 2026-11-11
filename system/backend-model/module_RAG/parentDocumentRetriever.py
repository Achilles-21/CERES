from langchain.storage import InMemoryStore
from langchain_chroma import Chroma
from langchain.retrievers import ParentDocumentRetriever
import pickle

from server.config import set_environment
from langchain_community.document_loaders import TextLoader
from langchain_openai import OpenAIEmbeddings
from langchain_text_splitters import RecursiveCharacterTextSplitter
import os
from pathlib import Path


if __name__ == "__main__":
    docs_path = os.environ["CERES_DOCUMENTS_DIR"]
    current_file_path = Path(__file__).resolve()
    current_dir = current_file_path.parent
    vectorstore_path = current_dir / "vectors"
    doc_names = os.listdir(docs_path)
    loaders = [TextLoader(Path(docs_path) / doc_name) for doc_name in doc_names]
    docs = []
    for loader in loaders:
        docs.extend(loader.load())
    docs_folder = current_dir / "docs"
    docs_folder.mkdir(exist_ok=True)
    docstore_path = current_dir / "docs" / "docstore.pkl"

    parent_splitter = RecursiveCharacterTextSplitter(chunk_size=2160)
    child_splitter = RecursiveCharacterTextSplitter(chunk_size=1080)

    embedding = OpenAIEmbeddings()
    vectorstore = Chroma(
                            collection_name="split_parents",
                            embedding_function=embedding,
                            persist_directory=str(vectorstore_path)
                        )

    if os.path.exists(docstore_path):
        with open(docstore_path, "rb") as f:
            store = pickle.load(f)
    else:
        store = InMemoryStore()

    retriever = ParentDocumentRetriever(
                                            vectorstore=vectorstore,
                                            docstore=store,
                                            child_splitter=child_splitter,
                                            parent_splitter=parent_splitter,
                                        )


    retrieved_docs = retriever.invoke("Cotton wool spots")
    print(retrieved_docs[0].page_content)
