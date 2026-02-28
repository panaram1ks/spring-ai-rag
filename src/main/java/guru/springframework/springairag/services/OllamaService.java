package guru.springframework.springairag.services;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;

public interface OllamaService {
    Answer getAnswer(Question question);
}
