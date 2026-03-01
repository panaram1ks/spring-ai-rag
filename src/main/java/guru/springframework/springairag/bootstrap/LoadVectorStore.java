package guru.springframework.springairag.bootstrap;

import guru.springframework.springairag.config.VectorStoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@RequiredArgsConstructor
@Component
public class LoadVectorStore implements CommandLineRunner {

    private static final String COLLECTION_NOT_EXISTS = "CollectionNotExists";

    private final VectorStore vectorStore;
    private final VectorStoreProperties vectorStoreProperties;

//    private boolean isCollectionNotExists(Throwable t) {
//        while (t != null) {
//            if (t.getMessage() != null && t.getMessage().contains(COLLECTION_NOT_EXISTS)) {
//                return true;
//            }
//            t = t.getCause();
//        }
//        return false;
//    }

    private boolean isStoreEmpty() {
        try {
            return vectorStore.similaritySearch("Sportsman").isEmpty();
        } catch (Exception e) {
//            if (isCollectionNotExists(e)) {
//                return true;
//            }
            return false;
        }
    }

    @Override
    public void run(String... args) throws Exception {

        if (isStoreEmpty()) {
            System.out.println("Loading documents into vector store");

            vectorStoreProperties.getDocumentsToLoad().forEach(document -> {
                System.out.println("Loading document: " + document.getFilename());

                TikaDocumentReader documentReader = new TikaDocumentReader(document);
                List<Document> documents = documentReader.get();

                TextSplitter textSplitter = new TokenTextSplitter();

                List<Document> splitDocuments = textSplitter.apply(documents);

                vectorStore.add(splitDocuments);
            });
        }

        System.out.println("Vector store loaded");
    }
}
