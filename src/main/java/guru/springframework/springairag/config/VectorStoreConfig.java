package guru.springframework.springairag.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;


@Slf4j
@Configuration
public class VectorStoreConfig {

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel, VectorStoreProperties vectorStoreProperties) {

        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();

        File vectorStoreFile = new File(vectorStoreProperties.getVectorStorePath());

        if (vectorStoreFile.exists()) {
            simpleVectorStore.load(vectorStoreFile);
        } else {
            log.info("Loading document into vector store");
            System.out.println("Loading document into vector store");
            vectorStoreProperties.getDocumentsToLoad()
                    .forEach(document -> {
                                log.info(document.getFilename());
                                System.out.println(document.getFilename());
                                TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(document);
                                List<Document> documents = tikaDocumentReader.get();
                                TextSplitter textSplitter = new TokenTextSplitter();
                                List<Document> splitDocs = textSplitter.apply(documents);
                                try {
                                    simpleVectorStore.add(splitDocs);
                                } catch (RuntimeException e) {
                                    log.warn("Failed to embed document chunk, skipping: " + e.getMessage());
                                }
                            }
                    );
            simpleVectorStore.save(vectorStoreFile);
        }
        return simpleVectorStore;
    }
}
