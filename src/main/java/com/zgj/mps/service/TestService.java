package com.zgj.mps.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestService {

//    @Autowired
//    private RabbitMqManager rabbitMqManager;

    public void sendCmd(){
//        rabbitMqManager.sendCmd(IdUtil.simpleUUID());
//        TerminalSetting terminalSetting = new TerminalSetting();
//
//        com.seeyoo.mps.model.Terminal terminal = new com.seeyoo.mps.model.Terminal();
//        terminal.setId(1l);
//
//        TerminalSettingKey terminalSettingKey = new TerminalSettingKey(terminal, TerminalSettingEnum.PLAYCONTROL);
//        terminalSetting.setTerminalSettingKey(terminalSettingKey);
//        terminalSetting.setSettings("{11123}");
//        terminalSettingRepository.save(terminalSetting);
    }

//    public void sendTask(){
//        rabbitMqManager.sendTask(IdUtil.simpleUUID());
//    }

    public static void main(String[] args) {
        SimpleHash hash = new SimpleHash("md5", "3f6974d83ac15f834f0457b4581ad963",
                "abdefeacd7a345860276994e6bffc805", 2);
        String encodedPassword = hash.toHex();
        System.out.println(encodedPassword);

    }
}
