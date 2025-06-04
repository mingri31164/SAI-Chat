package com.mingri.ai.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiFacadeService {
    @Autowired
    private RagServiceImpl ragService;
    @Autowired
    private ImageAiService imageAiService;
    @Autowired
    private AudioAiService audioAiService;

    public String chat(String userQuery) {
        return ragService.chatWithRag(userQuery);
    }

    public String analyzeImage(byte[] imageBytes) {
        return imageAiService.analyzeImage(imageBytes);
    }

    public String transcribeAudio(byte[] audioBytes) {
        return audioAiService.transcribeAudio(audioBytes);
    }
} 