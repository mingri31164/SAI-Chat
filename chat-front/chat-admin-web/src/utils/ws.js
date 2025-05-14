import EventBus from "@/utils/eventBus.js";

let ws = null;
let heartTimer = null;
let timer = null;
let lockReconnect = false;
let token = null;
const reconnectCountMax = 200;
let reconnectCount = 0;
let isConnect = false;

function response(event) {
    if (event.type !== 'message') {
        console.log(event);
        onCloseHandler();
        return;
    }
    let wsContent;
    try {
        wsContent = JSON.parse(event.data);
    } catch (e) {
        onCloseHandler();
        return;
    }
    if (wsContent.type) {
        if (wsContent.data && wsContent.data.code === -1) {
            onCloseHandler();
        } else {
            switch (wsContent.type) {
                case "msg": {
                    EventBus.emit('on-receive-msg', wsContent.content)
                    break;
                }
                case "notify": {
                    break;
                }
                case "video": {
                    break;
                }
            }
        }
    } else {
        onCloseHandler();
    }
}

function connect(tokenStr) {
    if (isConnect || ws) return;
    isConnect = true;
    token = tokenStr;
    try {
        let wsIp = localStorage.getItem("serverWs");
        wsIp = wsIp ? wsIp : "ws://127.0.0.1:9100";
        ws = new WebSocket(wsIp + "/ws?x-token=" + token);

        ws.onopen = () => {
            console.log("Connected to server");
            clearTimer();
            sendHeartPack();
        };

        ws.onmessage = response;
        ws.onclose = onCloseHandler;
        ws.onerror = onCloseHandler;
    } catch (e) {
        onCloseHandler();
    }
}

function send(msg) {
    if (ws && ws.readyState === WebSocket.OPEN) ws.send(msg);
}

const sendHeartPack = () => {
    heartTimer = setInterval(() => {
        send("heart");
    }, 9900);
}

const onCloseHandler = () => {
    clearHeartPackTimer();
    if (ws) {
        ws.close();
        ws = null;
    }
    isConnect = false;
    if (lockReconnect) return;
    lockReconnect = true;
    if (timer) {
        clearTimeout(timer);
        timer = null;
    }
    if (reconnectCount >= reconnectCountMax) {
        reconnectCount = 0;
        return;
    }
    timer = setTimeout(() => {
        connect(token);
        reconnectCount++;
        lockReconnect = false;
    }, 5000);
}

const clearHeartPackTimer = () => {
    console.log("Closing connection");
    if (heartTimer) {
        clearInterval(heartTimer);
        heartTimer = null;
    }
}

const clearTimer = () => {
    if (timer) {
        clearInterval(timer);
        timer = null;
    }
}

export default {connect};