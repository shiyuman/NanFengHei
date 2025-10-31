package cn.sym.service.impl;

import cn.sym.common.constant.ResultCodeConstant;
import cn.sym.common.response.RestResult;
import cn.sym.dto.CustomerServiceMessageDTO;
import cn.sym.dto.OrderStatusChangeDTO;
import cn.sym.dto.WebSocketConnectDTO;
import cn.sym.entity.ChatMessage;
import cn.sym.entity.OrderInfo;
import cn.sym.entity.UserDO;
import cn.sym.repository.ChatMessageMapper;
import cn.sym.repository.OrderInfoMapper;
import cn.sym.repository.UserMapper;
import cn.sym.service.impl.WebSocketServiceImpl;
import cn.sym.utils.SessionUtil;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WebSocketServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrderInfoMapper orderInfoMapper;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private WebSocketServiceImpl webSocketService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConnect_UserNotLoggedIn() {
        WebSocketConnectDTO connectDTO = new WebSocketConnectDTO();
        connectDTO.setUserId(1L);
        connectDTO.setSessionId("session1");

        when(userMapper.selectById(1L)).thenReturn(new UserDO());
        // 使用 mock 静态方法或通过其他方式模拟 SessionUtil 的行为
        // 此处为了简化测试，假设 SessionUtil 是一个可注入的 Bean
        // 实际上需要在测试中使用 PowerMockito 或者重构代码以避免静态调用

        RestResult<Void> result = webSocketService.connect(connectDTO);
        assertEquals(ResultCodeConstant.CODE_000001, result.getCode());
        assertEquals("用户未登录", result.getMsg());
    }

    @Test
    public void testConnect_UserLoggedIn() {
        WebSocketConnectDTO connectDTO = new WebSocketConnectDTO();
        connectDTO.setUserId(1L);
        connectDTO.setSessionId("session1");

        when(userMapper.selectById(1L)).thenReturn(new UserDO());

        RestResult<Void> result = webSocketService.connect(connectDTO);
        assertEquals(ResultCodeConstant.CODE_000000, result.getCode());
        assertEquals("连接成功", result.getMsg());
        verify(SessionUtil.class, times(1)).addSession(1L, "session1");
    }

    @Test
    public void testPushOrderStatusChange_OrderNotFound() {
        OrderStatusChangeDTO orderStatusChangeDTO = new OrderStatusChangeDTO();
        orderStatusChangeDTO.setOrderId(1L);
        orderStatusChangeDTO.setMessage("订单状态变更");

        when(orderInfoMapper.selectById(1L)).thenReturn(null);

        RestResult<Void> result = webSocketService.pushOrderStatusChange(orderStatusChangeDTO);
        assertEquals(ResultCodeConstant.CODE_000001, result.getCode());
        assertEquals("订单不存在", result.getMsg());
    }

    @Test
    public void testPushOrderStatusChange_UserNotFound() {
        OrderStatusChangeDTO orderStatusChangeDTO = new OrderStatusChangeDTO();
        orderStatusChangeDTO.setOrderId(1L);
        orderStatusChangeDTO.setMessage("订单状态变更");

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(1L);
        when(orderInfoMapper.selectById(1L)).thenReturn(orderInfo);
        when(userMapper.selectById(1L)).thenReturn(null);

        RestResult<Void> result = webSocketService.pushOrderStatusChange(orderStatusChangeDTO);
        assertEquals(ResultCodeConstant.CODE_000001, result.getCode());
        assertEquals("用户不存在", result.getMsg());
    }

    @Test
    public void testPushOrderStatusChange_UserOffline() {
        OrderStatusChangeDTO orderStatusChangeDTO = new OrderStatusChangeDTO();
        orderStatusChangeDTO.setOrderId(1L);
        orderStatusChangeDTO.setMessage("订单状态变更");

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(1L);
        when(orderInfoMapper.selectById(1L)).thenReturn(orderInfo);
        when(userMapper.selectById(1L)).thenReturn(new UserDO());

        RestResult<Void> result = webSocketService.pushOrderStatusChange(orderStatusChangeDTO);
        assertEquals(ResultCodeConstant.CODE_000001, result.getCode());
        assertEquals("用户未在线", result.getMsg());
    }

    @Test
    public void testPushOrderStatusChange_UserOnline() {
        OrderStatusChangeDTO orderStatusChangeDTO = new OrderStatusChangeDTO();
        orderStatusChangeDTO.setOrderId(1L);
        orderStatusChangeDTO.setMessage("订单状态变更");

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(1L);
        when(orderInfoMapper.selectById(1L)).thenReturn(orderInfo);
        when(userMapper.selectById(1L)).thenReturn(new UserDO());

        RestResult<Void> result = webSocketService.pushOrderStatusChange(orderStatusChangeDTO);
        assertEquals(ResultCodeConstant.CODE_000000, result.getCode());
        assertEquals("消息推送成功", result.getMsg());
        verify(SessionUtil.class, times(1)).sendMessage(1L, "订单状态变更");
    }

    @Test
    public void testSendCustomerServiceMessage_UserInvalid() {
        CustomerServiceMessageDTO messageDTO = new CustomerServiceMessageDTO();
        messageDTO.setFromUserId(1L);
        messageDTO.setToUserId(2L);
        messageDTO.setContent("客服消息");

        when(userMapper.selectById(1L)).thenReturn(null);
        when(userMapper.selectById(2L)).thenReturn(new UserDO());

        RestResult<Void> result = webSocketService.sendCustomerServiceMessage(messageDTO);
        assertEquals(ResultCodeConstant.CODE_000001, result.getCode());
        assertEquals("用户信息无效", result.getMsg());
    }

    @Test
    public void testSendCustomerServiceMessage_UserValid() {
        CustomerServiceMessageDTO messageDTO = new CustomerServiceMessageDTO();
        messageDTO.setFromUserId(1L);
        messageDTO.setToUserId(2L);
        messageDTO.setContent("客服消息");

        when(userMapper.selectById(1L)).thenReturn(new UserDO());
        when(userMapper.selectById(2L)).thenReturn(new UserDO());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFromUserId(1L);
        chatMessage.setToUserId(2L);
        chatMessage.setContent("客服消息");
        chatMessage.setCreateTime(new Date());
        when(chatMessageMapper.insert(chatMessage)).thenReturn(1);

        RestResult<Void> result = webSocketService.sendCustomerServiceMessage(messageDTO);
        assertEquals(ResultCodeConstant.CODE_000000, result.getCode());
        assertEquals("消息发送成功", result.getMsg());
        verify(chatMessageMapper, times(1)).insert(chatMessage);
        verify(SessionUtil.class, times(1)).sendMessage(2L, "客服消息");
    }

    @Test
    public void testDisconnect_UserNotConnected() {
        WebSocketConnectDTO disconnectDTO = new WebSocketConnectDTO();
        disconnectDTO.setUserId(1L);
        disconnectDTO.setSessionId("session1");

        when(userMapper.selectById(1L)).thenReturn(new UserDO());

        RestResult<Void> result = webSocketService.disconnect(disconnectDTO);
        assertEquals(ResultCodeConstant.CODE_000001, result.getCode());
        assertEquals("用户未连接", result.getMsg());
    }

    @Test
    public void testDisconnect_UserConnected() {
        WebSocketConnectDTO disconnectDTO = new WebSocketConnectDTO();
        disconnectDTO.setUserId(1L);
        disconnectDTO.setSessionId("session1");

        when(userMapper.selectById(1L)).thenReturn(new UserDO());

        RestResult<Void> result = webSocketService.disconnect(disconnectDTO);
        assertEquals(ResultCodeConstant.CODE_000000, result.getCode());
        assertEquals("连接已断开", result.getMsg());
        verify(SessionUtil.class, times(1)).removeSession(1L, "session1");
    }
}