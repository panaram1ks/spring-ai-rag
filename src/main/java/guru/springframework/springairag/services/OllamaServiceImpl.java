package guru.springframework.springairag.services;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OllamaServiceImpl implements OllamaService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    @Value("classpath:/templates/rag-promt-meta-data.st")
    private Resource ragPromptTemplate;

    @Value("classpath:/templates/system-message.st")
    private Resource systemMessageTemplate;

    @Override
    public Answer getAnswer(Question question) {
        PromptTemplate systemMessagePromptTemplate = new PromptTemplate(systemMessageTemplate);
        Message systemMessage = systemMessagePromptTemplate.createMessage();

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(question.question()).topK(5).build()
        );
        List<String> contentList = documents.stream().map(Document::getContent).toList();
        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
        Message userMessage = promptTemplate.createMessage(Map.of(
                "input", question.question(),
                "documents",  String.join("\n", contentList)));


//        List<Document> documents = vectorStore.similaritySearch(
//                SearchRequest.builder()
//                        .query(question.question())
//                        .topK(6)
//                        .build()
//        );
//        List<String> contentList = documents.stream().map(Document::getContent).toList();
//        System.out.println(contentList);
//        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
////        PromptTemplate promptTemplate = new PromptTemplate(question.question());
//        Prompt prompt = promptTemplate.create(Map.of(
//                "input", question.question(),
//                "documents",  String.join("\n", contentList)));
////        Prompt prompt = promptTemplate.create();
//        ChatResponse chatResponse = chatModel.call(prompt);

        System.out.println("userMessage " + userMessage);
        ChatResponse chatResponse = chatModel.call(new Prompt(List.of(systemMessage, userMessage)));
        return new Answer(chatResponse.getResult().getOutput().getContent());
    }


}