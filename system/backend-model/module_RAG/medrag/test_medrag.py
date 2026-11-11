from src.medrag import MedRAG

if __name__ == "__main__":





    medrag = MedRAG(rag=True, retriever_name="RRF-4", follow_up=True, corpus_name="Textbooks")
    print(medrag.answer(question="the nasolacrimal canal extends into what part of the nose?", options=None, n_rounds=2, n_queries=2, save_path="./messages"))
