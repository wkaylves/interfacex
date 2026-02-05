package com.kaylves.interfacex.ui.form;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RocketmqActionListener implements ActionListener {

    private RocketMQForm rocketMQForm;

    public RocketmqActionListener(RocketMQForm rocketMQForm) {
        this.rocketMQForm = rocketMQForm;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String producerGroup = "InterfaceX";
        String namesrvAddr = rocketMQForm.getServerTxt().getText();
        String topic = rocketMQForm.getTopicTxt().getText();
        String tag = rocketMQForm.getTagTxt().getText();
        String body = rocketMQForm.getBodyEditorPanel().getText();
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);

        try {

            producer.setNamesrvAddr(namesrvAddr);
            producer.start();
            Message msg = new Message(topic, tag, body.getBytes());
            SendResult sendResult = producer.send(msg);


            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            String resultJson = gson.toJson(sendResult);

            String s = "<html><body>"+resultJson+"</body></html>";
            rocketMQForm.getResultEditorPanel().setContentType("text/html");

            rocketMQForm.getResultEditorPanel().setText(s);

        } catch (Exception ex) {
            String s = "<html><body>"+ex.getMessage()+"</body></html>";
            rocketMQForm.getResultEditorPanel().setContentType("text/html");
            rocketMQForm.getResultEditorPanel().setText(s);

        }  finally {
            producer.shutdown();
        }

    }
}
