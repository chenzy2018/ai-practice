package com.czy.qwen.dto;

import java.util.List;

/**
 * 通义千问请求 DTO
 *
 * @author chenzhenyu 2026年05月14日
 */
public class QwenRequest {

    private String model;
    private Input input;
    private Parameters parameters;

    public QwenRequest() {
    }

    public QwenRequest(String model, List<Message> messages, Double temperature) {
        this.model = model;
        this.input = new Input(messages);
        this.parameters = new Parameters(temperature);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public static class Input {
        private List<Message> messages;

        public Input() {
        }

        public Input(List<Message> messages) {
            this.messages = messages;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }
    }

    public static class Parameters {
        private Double temperature;
        private Double top_p = 0.8;
        private Integer max_tokens = 1024;

        public Parameters() {
        }

        public Parameters(Double temperature) {
            this.temperature = temperature;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getTop_p() {
            return top_p;
        }

        public void setTop_p(Double top_p) {
            this.top_p = top_p;
        }

        public Integer getMax_tokens() {
            return max_tokens;
        }

        public void setMax_tokens(Integer max_tokens) {
            this.max_tokens = max_tokens;
        }
    }
}