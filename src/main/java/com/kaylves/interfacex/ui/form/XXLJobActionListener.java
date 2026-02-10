package com.kaylves.interfacex.ui.form;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.client.ExecutorBizClient;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public  class XXLJobActionListener implements ActionListener {

    XXLJobForm xxlJobFrom;

    public XXLJobActionListener(XXLJobForm xxlJobFrom) {
        this.xxlJobFrom = xxlJobFrom;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //
        reqClick();
    }

    public void reqClick() {
        String addressUrl = xxlJobFrom.getAddressTxt().getText();
        String accessToken=null;

        ExecutorBiz executorBiz = new ExecutorBizClient(addressUrl, accessToken);

        // trigger data
        final TriggerParam triggerParam = new TriggerParam();
        triggerParam.setJobId(1);
        triggerParam.setExecutorHandler(xxlJobFrom.getInterfaceXItem().getUrl());
        triggerParam.setExecutorParams(xxlJobFrom.getParamTxtArea().getText());
        triggerParam.setExecutorBlockStrategy(ExecutorBlockStrategyEnum.COVER_EARLY.name());
        triggerParam.setGlueType(GlueTypeEnum.BEAN.name());
        triggerParam.setGlueSource(null);
        triggerParam.setGlueUpdatetime(System.currentTimeMillis());
        triggerParam.setLogDateTime(System.currentTimeMillis());

        // Act
        final ReturnT<String> retval = executorBiz.run(triggerParam);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String resultJson = gson.toJson(retval);

        String s = "<html><body>"+resultJson+"</body></html>";
        xxlJobFrom.getResultEditorPane().setContentType("text/html");
        xxlJobFrom.getResultEditorPane().setText(s);
        xxlJobFrom.getTabbedPane1().setSelectedIndex(1);


    }
}
