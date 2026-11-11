from langchain_community.document_loaders import TextLoader
from langchain_openai import OpenAIEmbeddings
from langchain_text_splitters import RecursiveCharacterTextSplitter
import os
from pathlib import Path
from langchain_community.vectorstores import FAISS
import chardet

if __name__ == "__main__":
    vectorstore_path = Path(os.environ["CERES_VECTORSTORE_DIR"])
    vectorstore_path.mkdir(exist_ok=True)

    #         loaders.append(TextLoader(doc_path, encoding=encoding))

    embeddings = OpenAIEmbeddings()


    vectorstore = FAISS.load_local(str(vectorstore_path), embeddings, allow_dangerous_deserialization=True)
    retriever = vectorstore.as_retriever(search_type="similarity", search_kwargs={"k": 3})
    docs = retriever.invoke("Wills Eye Hospital?")
    print(docs)
