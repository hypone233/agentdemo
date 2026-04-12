package com.zzz.aidemo.agent;


import com.zzz.aidemo.agent.model.AgentStepResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EqualsAndHashCode(callSuper = true)
@Data

public abstract class ReActAgent extends BaseAgent {

    private static final Logger logger = LoggerFactory.getLogger(ReActAgent.class);

    public abstract boolean think();

    public abstract String act();

    @Override
    public AgentStepResult step() {
        try {
            boolean shouldAct = think();

            // 不需要行动，说明当前轮可以结束
            if (!shouldAct) {
                return AgentStepResult.finish("思考完成，无需继续行动");
            }

            // 需要行动时，执行 act
            String actionResult = act();


            return AgentStepResult.finish(actionResult);
        } catch (Exception e) {
            logger.error("ReAct step execute error", e);
            return AgentStepResult.finish("步骤执行失败：" + e.getMessage());
        }
    }
}
