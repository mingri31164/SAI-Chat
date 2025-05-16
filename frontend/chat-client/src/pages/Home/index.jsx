import "./index.less"
import {useEffect, useRef, useState} from "react";
import {Redirect, Route, Switch, useHistory} from "react-router-dom";
import Chat from "./Chat/index.jsx";
import Friend from "./Friend/index.jsx";
import Set from "./Set/index.jsx";
import Talk from "./Talk/index.jsx";
import WindowOperation from "../../componets/WindowOperation/index.jsx";
import CustomDragDiv from "../../componets/CustomDragDiv/index.jsx";
import ws from "../../utils/ws.js";
import {invoke} from "@tauri-apps/api/core";
import {useDispatch, useSelector} from "react-redux";
import {setCurrentLoginUserInfo, setCurrentOption} from "../../store/home/action.js";
import {WebviewWindow} from "@tauri-apps/api/WebviewWindow";
import CreateTrayWindow from "../TrayMenu/window.jsx";
import Notify from "./Notify/index.jsx";
import UserApi from "../../api/user.js";
import {emit, listen} from "@tauri-apps/api/event";
import CrateMessageBox from "../MessageBox/window.jsx";
import {addChatWindowUser, deleteChatWindowUser, setCurrentChatId} from "../../store/chat/action.js";
import ChatListApi from "../../api/chatList.js";
import CreateVideoChat from "../VideoChat/window.jsx";
import CustomModal from "../../componets/CustomModal/index.jsx";
import IconButton from "../../componets/IconButton/index.jsx";
import CustomInput from "../../componets/CustomInput/index.jsx";
import CustomButton from "../../componets/CustomButton/index.jsx";
import {formatDateString} from "../../utils/date.js";
import Dropzone from "react-dropzone";
import {useToast} from "../../componets/CustomToast/index.jsx";
import RightClickContent from "../../componets/RightClickContent/index.jsx";
import CustomLine from "../../componets/CustomLine/index.jsx";
import UserSetApi from "../../api/userSet.js";
import {shortcutRegisterAndEmit} from "../../utils/shortcut.js";
import {setItem} from "../../utils/storage.js";
import CreateScreenshot from "../screenshot/window.jsx";
import CreateAboutWindow from "../AboutWindow/window.jsx";
import CreateImageViewer from "../ImageViewer/window.jsx";
import {getFileNameAndType} from "../../utils/string.js";
import CreateCmdWindow from "../Command/window.jsx";
import CreateChatWindow from "../ChatWindow/window.jsx";
import CustomBox from "../../componets/CustomBox/index.jsx";
import CreateLogin from "../Login/window.jsx";
import {check} from "@tauri-apps/plugin-updater";
import {relaunch} from "@tauri-apps/plugin-process";
import ProgressBar from "../../componets/ProgressBar/index.jsx";
import {JSEncrypt} from "jsencrypt";
import LoginApi from "../../api/login.js";

export default function Home() {
    const homeStoreData = useSelector(store => store.homeData)
    const chatStoreData = useSelector((state) => state.chatData)
    const currentOption = useRef(homeStoreData.currentOption)
    const currentToId = useRef(chatStoreData.currentChatId)//消息目标用户
    const [selectedOptionIndex, setSelectedOptionIndex] = useState("chat")
    const h = useHistory();
    const dispatch = useDispatch();
    const [unreadInfo, setUnreadInfo] = useState({})
    const [userInfo, setUserInfo] = useState({name: "", signature: "", sex: ""})
    const [userUpdatePassword, setUserPassword] = useState({oldPassword: "", newPassword: "", confirmPassword: ""})
    const [isOpenEditInfo, setIsOpenEditInfo] = useState(false)
    const [isOpenPasswordEdit, setisOpenPasswordEdit] = useState(false)
    const userInfoBackCache = useRef(null)
    let showToast = useToast()
    const [userInfoPosition, setUserInfoPosition] = useState(null)
    const [userInfoVisible, setUserInfoVisible] = useState(false)
    const [isUpdaterOpen, setIsUpdaterOpen] = useState(false)
    const [updaterProgress, setUpdaterProgress] = useState(0)
    const [updating, SetUpdating] = useState(false)
    const [updater, setUpdater] = useState(null)

    useEffect(() => {
        (async () => {
            try {
                let result = await check();
                if (result.available) {
                    setUpdater(result)
                    setIsUpdaterOpen(true)
                }
            } catch (e) {
                setIsUpdaterOpen(false)
            }
        })();
    }, [])

    const onUpdater = async () => {
        SetUpdating(true)
        try {
            let downloaded = 0;
            let contentLength = 0;
            await updater.downloadAndInstall((event) => {
                switch (event.event) {
                    case 'Started':
                        contentLength = event.data.contentLength
                        break;
                    case 'Progress':
                        downloaded += event.data.chunkLength
                        setUpdaterProgress(downloaded / contentLength * 100)
                        break;
                    case 'Finished':
                        SetUpdating(false)
                        setUpdaterProgress(0)
                        setIsUpdaterOpen(false)
                        break;
                }
            });
            await relaunch();
        } catch (e) {
            showToast("更新失败~", true)
            setIsUpdaterOpen(false)
        } finally {
            setUpdater(null)
        }
    }

    useEffect(() => {
        const cleanupFunctions = [];

        const setup = async () => {
            try {
                // 获取用户信息并初始化
                const res = await invoke("get_user_info", {});
                dispatch(setCurrentLoginUserInfo(res.user_id, res.username, res.account, res.portrait));
                let token = res.token;
                userInfoBackCache.current = res;
                if (token) {
                    ws.connect(token);
                    CreateTrayWindow();
                    CrateMessageBox();
                    CreateCmdWindow();
                    onGetUserUnreadNum();
                    onGetUserSet();
                }

                const appWindow = await WebviewWindow.getByLabel('home');

                // 监听隐藏或显示 home 窗口
                const unHideOrShowHome = await listen("hideOrShowHome", async function (e) {
                    let isVisible = await appWindow.isVisible();
                    if (isVisible) {
                        await appWindow.hide();
                    } else {
                        await appWindow.setFocus();
                        await appWindow.unminimize();
                        await appWindow.show();
                    }
                });
                cleanupFunctions.push(unHideOrShowHome);

                // 监听截图快捷键事件
                const unScreenshot = await listen('screenshot', async (event) => {
                    if (currentOption.current !== 'chat' || !currentToId.current) {
                        CreateScreenshot("");
                    }
                });
                cleanupFunctions.push(unScreenshot);

                // 监听命令行模式
                const unCmd = await listen('command', async (event) => {
                    CreateCmdWindow();
                });
                cleanupFunctions.push(unCmd);

            } catch (error) {
                console.error("Error in useEffect setup:", error);
            }
        };

        setup();

        // 清理函数
        return () => {
            cleanupFunctions.forEach(cleanup => {
                if (typeof cleanup === 'function') {
                    cleanup();
                } else if (cleanup && typeof cleanup.then === 'function') {
                    cleanup.then(fn => fn && fn());
                }
            });
        };
    }, []);

    const onGetChatList = () => {
        ChatListApi.list().then(res => {
            if (res.code === 0) {
                emit("chat-list", [...res.data.tops, ...res.data.others])
            }
        })
    }

    const registerAllShortcut = async (value) => {
        //cmd
        shortcutRegisterAndEmit(value.command, "command")
        //screenshot
        shortcutRegisterAndEmit(value.screenshot, "screenshot")
        //hideOrShowHome
        shortcutRegisterAndEmit(value.hideOrShowHome, "hideOrShowHome")
        //openUnreadMsg
        shortcutRegisterAndEmit(value.openUnreadMsg, "openUnreadMsg")
        //closeMsgWindow
        shortcutRegisterAndEmit(value.closeMsgWindow, "closeMsgWindow")
    }

    const onGetUserSet = () => {
        UserSetApi.getUserSet().then(res => {
            if (res.code === 0) {
                registerAllShortcut(res.data.sets)
                setItem("user-sets", res.data.sets)
            }
        })

    }

    useEffect(() => {
        //监听后端接受到的消息
        const unMsgListen = listen('on-receive-msg', (event) => {
            onGetChatList()
            if (currentOption.current !== "chat") {
                onGetUserUnreadNum()
                return
            }
            let data = event.payload
            if (currentToId.current !== data.fromId) {
                onGetUserUnreadNum()
            }
        });
        const unChatDestroyed = listen('chat-destroyed', (event) => {
            dispatch(deleteChatWindowUser(event.payload.fromId))
        })
        const unChatNew = listen('chat-new', (event) => {
            let data = event.payload
            dispatch(addChatWindowUser(data))
            let title = data.remark ? data.remark : data.name
            CreateChatWindow(data.fromId, title ? title : "linyu", data.type)
        })
        const unUnreadListen = listen('on-unread-info', (event) => {
            onGetUserUnreadNum()
        });
        const unNotifyListen = listen('on-receive-notify', (event) => {
            if (currentOption.current !== "notify") {
                onGetChatList()
                onGetUserUnreadNum()
            }
        });
        const unVideoListen = listen('on-receive-video', async (event) => {
            let data = event.payload
            if (data.type === "invite") {
                CreateVideoChat(data.fromId, false, data.isOnlyAudio)
            }
        });
        let unChatListJumpListen = listen('chat-list-jump', async (event) => {
            let info = event.payload
            const window = await WebviewWindow.getByLabel('home')
            window.show()
            window.unminimize()
            window.setFocus()
            dispatch(setCurrentChatId(info.fromId, info))
            dispatch(setCurrentOption("chat"))
            h.push("/home/chat")
        })
        return async () => {
            (await unMsgListen)();
            (await unUnreadListen)();
            (await unNotifyListen)();
            (await unVideoListen)();
            (await unChatListJumpListen)();
            (await unChatDestroyed)();
            (await unChatNew)();
        }
    }, [])

    let onGetUserUnreadNum = () => {
        UserApi.unread().then(res => {
            if (res.code === 0) {
                setUnreadInfo(res.data)
            }
        })
    }

    useEffect(() => {
        currentOption.current = homeStoreData.currentOption
        if (homeStoreData.currentOption)
            setSelectedOptionIndex(homeStoreData.currentOption)
    }, [homeStoreData.currentOption])


    useEffect(() => {
        currentToId.current = chatStoreData.currentChatId
    }, [chatStoreData.currentChatId])

    useEffect(() => {
        const handleEscKey = (event) => {
            if (event.key === 'Escape' || event.keyCode === 27) {
                WebviewWindow.getCurrent().hide()
            }
        };
        document.addEventListener('keydown', handleEscKey);
        return () => {
            document.removeEventListener('keydown', handleEscKey);
        };
    }, [])

    const options = [
        {key: "chat", icon: "chat", page: "/home/chat"},
        {key: "friend", icon: "user", page: "/home/friend"},
        {key: "talk", icon: "talk", page: "/home/talk"},
        {key: "notify", icon: "notify", page: "/home/notify"},
        {key: "set", icon: "set", page: "/home/set"},
    ]

    const onGetUserInfo = (e) => {
        UserApi.info().then(res => {
            if (res.code === 0) {
                setUserInfo(res.data)
                setUserInfoPosition({x: e.clientX, y: e.clientY})
            }
        })
    }

    const updateUserInfoCache = (info) => {
        userInfoBackCache.current = info
        invoke('save_user_info', info)
        dispatch(setCurrentLoginUserInfo(info.userid, info.username, userInfo.account, info.portrait))
        emit("user-info-reload", info)
    }

    const onAvatarChange = (file) => {
        UserApi.upload(file).then(res => {
            if (res.code === 0) {
                setUserInfo({...userInfo, portrait: res.data})
                showToast("头像修改成功~")
                let info = {
                    userid: userInfoBackCache.current.user_id,
                    username: userInfo.name,
                    token: userInfoBackCache.current.token,
                    portrait: res.data,
                }
                updateUserInfoCache(info)
            }
        })
    }

    const onEditInfo = async () => {
        UserApi.update(userInfo).then(res => {
            showToast("信息修改成功~")
            let info = {
                userid: userInfoBackCache.current.user_id,
                username: userInfo.name,
                token: userInfoBackCache.current.token,
                portrait: userInfo.portrait,
            }
            updateUserInfoCache(info)
        })
        setIsOpenEditInfo(false)
    }
    const onUpatePassword = async () => {
        if (userUpdatePassword.oldPassword.length===0||userUpdatePassword.newPassword.length===0||userUpdatePassword.confirmPassword.length===0 ){
            showToast("密码不能为空~", true)
            return
        }
        if (userUpdatePassword.newPassword !== userUpdatePassword.confirmPassword) {
            showToast("两次密码输入不一致~", true)
            return
        }
        if (userUpdatePassword.newPassword.length < 6) {
            showToast("密码长度不能小于6位~", true)
            return
        }
        const encrypt = new JSEncrypt();
        let keyData = await LoginApi.publicKey();
        if (keyData.code !== 0) {
            return;
        }
        encrypt.setPublicKey(keyData.data);
        // userUpdatePassword.confirmPassword = encrypt.encrypt(userUpdatePassword.password)
        UserApi.updatePassword({...userUpdatePassword,confirmPassword: encrypt.encrypt(userUpdatePassword.confirmPassword)}).then(res => {
            if (res.code === 0) {
                showToast("密码修改成功~")
                setisOpenPasswordEdit(false)
                CreateLogin()
            }else
                showToast(res.msg, true)
        })

    }

    return (
        <div>
            <CustomModal isOpen={isUpdaterOpen}>
                <div className="updater">
                    <img className="updater-icon" alt="" src="/updater.png"/>
                    <div style={{fontWeight: 600}}>
                        版本更新
                    </div>
                    <div style={{color: "#969696", fontSize: 12}}>
                        新版本{updater?.version}
                    </div>
                    <div className="updater-content">
                        <div>[更新内容]</div>
                        <div>{updater?.body}</div>
                    </div>
                    {updating && <ProgressBar progress={updaterProgress}/>}
                    {
                        !updating &&
                        <div className="updater-operate">
                            <CustomButton width={80} onClick={() => {
                                setIsUpdaterOpen(false)
                                setUpdater(null)
                            }} type="minor">稍后更新</CustomButton>
                            <CustomButton width={80} onClick={onUpdater}>立即更新</CustomButton>
                        </div>
                    }
                </div>
            </CustomModal>
            <RightClickContent position={userInfoPosition} visible={userInfoVisible}>
                <div className="user-info">
                    <div style={{display: "flex", height: 60, alignItems: "center", marginBottom: 10}}>
                        <img className="user-info-portrait"
                             alt=""
                             src={userInfo.portrait}
                             onClick={() => {
                                 CreateImageViewer(getFileNameAndType(userInfo.portrait).fileName, userInfo.portrait)
                             }}
                        />
                        <div style={{flex: 1, overflow: "hidden"}}>
                            <div className="ellipsis">{userInfo.name}</div>
                            <div className="ellipsis"
                                 style={{fontSize: 14, color: "#989898"}}>{userInfo.account}</div>
                        </div>
                    </div>
                    <CustomLine width={1}/>
                    <div style={{
                        display: "flex",
                        flexDirection: "column",
                        justifyContent: "space-between",
                        alignItems: "center",
                    }}>
                        <CustomButton
                            width={160}
                            style={{marginTop: 10}} o
                            onClick={() => {
                                setUserInfoVisible({value: false})
                                setIsOpenEditInfo(true)
                            }}
                        >
                            编辑信息
                        </CustomButton>
                        <CustomButton
                            width={160}
                            type="line"
                            style={{marginTop: 5}}
                            onClick={() => {
                                setUserInfoVisible({value: false})
                                h.replace('/home/talk/reload');
                                setTimeout(() => {
                                    h.replace("/home/talk/all", {targetId: userInfo.id})
                                });
                                dispatch(setCurrentOption("talk"))
                            }}
                        >
                            我的说说
                        </CustomButton>
                        <CustomButton
                            width={160}
                            type="line"
                            style={{marginTop: 10}} o
                            onClick={() => {
                                setUserInfoVisible({value: false})
                                setisOpenPasswordEdit(true)
                            }}
                        >
                            修改密码
                        </CustomButton>
                        <CustomButton
                            width={160}
                            type="line"
                            style={{marginTop: 5}}
                            onClick={() => {
                                CreateLogin()
                            }}
                        >
                            切换用户
                        </CustomButton>
                    </div>
                </div>
            </RightClickContent>
            <CustomBox className="home">
                <CustomDragDiv className="home-nav">
                    <div>
                        <CustomModal
                            isOpen={isOpenEditInfo}
                        >
                            <div className="edit-info">
                                <div className="edit-info-top">
                                    <div style={{fontSize: 12}}>
                                        编辑信息
                                    </div>
                                    <div style={{position: "absolute", right: 10}}>
                                        <IconButton
                                            danger
                                            icon={<i className={`iconfont icon-guanbi`} style={{fontSize: 20}}/>}
                                            onClick={() => setIsOpenEditInfo(false)}
                                        />
                                    </div>
                                </div>
                                <div style={{
                                    width: "90%",
                                    display: "flex",
                                    flexDirection: "column",
                                    justifyContent: "center",
                                    alignItems: "center"
                                }}>
                                    <Dropzone
                                        onDrop={(acceptedFiles) => onAvatarChange(acceptedFiles[0])}
                                        accept={
                                            {
                                                'image/*': ['.png'],
                                            }
                                        }
                                    >
                                        {({getRootProps, getInputProps}) => (
                                            <div {...getRootProps()}>
                                                <input {...getInputProps()} />
                                                <div className="portrait-info">
                                                    <img style={{width: 80, height: 80, borderRadius: 80}}
                                                         src={userInfo.portrait}
                                                         alt={userInfo.portrait}/>
                                                    <div className="portrait-info-cover">
                                                        <i className={`iconfont icon-xiangji`}
                                                           style={{color: "#fff", fontSize: 40}}/>
                                                    </div>
                                                </div>
                                            </div>
                                        )}
                                    </Dropzone>
                                    <div style={{
                                        display: "flex",
                                        marginBottom: 20,
                                        width: 160,
                                        justifyContent: "space-between"
                                    }}>
                                        <div
                                            className={`sex-info  ${userInfo.sex === "男" ? "nan" : ""}`}
                                            onClick={() => setUserInfo({...userInfo, sex: "男"})}
                                        >
                                            <i className={`iconfont icon-nan`}
                                               style={{marginRight: 5}}/>
                                            <div>男生</div>
                                        </div>
                                        <div
                                            className={`sex-info  ${userInfo.sex === "女" ? "nv" : ""}`}
                                            onClick={() => setUserInfo({...userInfo, sex: "女"})}
                                        >
                                            <i className={`iconfont icon-nv`}
                                               style={{marginRight: 5}}/>
                                            <div>女生</div>
                                        </div>
                                    </div>
                                    <div style={{width: "100%"}}>
                                        <div style={{marginBottom: 20}}>
                                            <CustomInput
                                                pre="昵称"
                                                value={userInfo.name}
                                                limit={30}
                                                onChange={(v) => setUserInfo({...userInfo, "name": v})}
                                            />
                                        </div>
                                        <div style={{marginBottom: 20}}>
                                            <CustomInput
                                                pre="签名"
                                                limit={100}
                                                value={userInfo.signature}
                                                onChange={(v) => setUserInfo({...userInfo, "signature": v})}
                                            />
                                        </div>
                                        <div style={{marginBottom: 20}}>
                                            <CustomInput
                                                value={formatDateString(userInfo.birthday)}
                                                pre="生日"
                                                type="date"
                                                onChange={(v) => setUserInfo({...userInfo, "birthday": v})}
                                            />
                                        </div>
                                    </div>
                                </div>
                                <div style={{display: "flex", position: "absolute", right: 10, bottom: 10}}>
                                    <CustomButton
                                        width={55}
                                        onClick={onEditInfo}
                                    >
                                        保存
                                    </CustomButton>
                                    <CustomButton
                                        width={55}
                                        type="minor"
                                        onClick={() => setIsOpenEditInfo(false)}
                                    >
                                        取消
                                    </CustomButton>
                                </div>
                            </div>
                        </CustomModal
                        >
                        <CustomModal
                            isOpen={isOpenPasswordEdit}
                        >
                            <div className="edit-info">
                                <div className="edit-info-top">
                                    <div style={{fontSize: 12}}>
                                        修改密码
                                    </div>

                                </div>
                                <div style={{
                                    width: "90%",
                                    display: "flex",
                                    flexDirection: "column",
                                    justifyContent: "center",
                                    alignItems: "center"
                                }}>

                                    <div style={{
                                        display: "flex",
                                        marginBottom: 20,
                                        width: 160,
                                        justifyContent: "space-between"
                                    }}>
                                    </div>
                                    <div style={{width: "100%"}}>
                                        <div style={{marginBottom: 20}}>
                                            <CustomInput
                                                placeholder="原密码"
                                                limit={16}
                                                required={true}
                                                requiredMsg="原密码不能为空"
                                                type="password"
                                                value={userUpdatePassword.oldPassword}
                                                onChange={(v) => setUserPassword({...userUpdatePassword, "oldPassword": v})}
                                            />
                                        </div>
                                        <div style={{marginBottom: 20}}>
                                            <CustomInput
                                                placeholder="新密码"
                                                limit={16}
                                                required={true}
                                                requiredMsg="新密码不能为空"
                                                type="password"
                                                // value={userInfo.signature}
                                                onChange={(v) => setUserPassword({...userUpdatePassword, "newPassword": v})}
                                            />
                                        </div>
                                        <div style={{marginBottom: 20}}>
                                            <CustomInput
                                                // value={formatDateString(userInfo.birthday)}
                                                placeholder="请确认"
                                                limit={16}
                                                required={true}
                                                requiredMsg="请再次输入密码"
                                                type="password"
                                                // type="date"
                                                onChange={(v) => setUserPassword({...userUpdatePassword, "confirmPassword": v})}
                                            />
                                        </div>
                                    </div>
                                </div>
                                <div style={{display: "flex", position: "absolute", right: 10, bottom: 10}}>
                                    <CustomButton
                                        width={55}
                                        onClick={onUpatePassword}
                                    >
                                        修改
                                    </CustomButton>
                                    <CustomButton
                                        width={55}
                                        type="minor"
                                        onClick={() => setisOpenPasswordEdit(false)}
                                    >
                                        取消
                                    </CustomButton>
                                </div>
                            </div>
                        </CustomModal
                        >
                    </div>
                    <div className="home-nav-icon" onClick={CreateAboutWindow}>
                        <img style={{height: 60}} src="/logo.png" alt=""/>
                    </div>
                    <div className="home-nav-options">
                        {
                            options.map((option) => {
                                return (
                                    <div
                                        key={option.key}
                                        className={`home-nav-option ${option.key === selectedOptionIndex ? "selected" : ""}`}
                                        onClick={() => {
                                            setSelectedOptionIndex(option.key)
                                            dispatch(setCurrentOption(option.key))
                                            h.push(option.page)
                                        }}

                                    >
                                        {/*<i className={`iconfont ${option.icon}`} style={{fontSize: 30}}/>*/}
                                        <img alt=""
                                             src={`/icon/${option.icon + (option.key === selectedOptionIndex ? "" : "-empty")}.png`}
                                             style={{width: 34, height: 34}}/>
                                        {
                                            unreadInfo[option.key] && unreadInfo[option.key] > 0 ?
                                                <div className="home-nav-option-tip">
                                                    {unreadInfo[option.key]}
                                                </div>
                                                : <></>
                                        }
                                    </div>
                                )
                            })
                        }
                    </div>
                    <div
                        className="home-nav-my"
                        onClick={onGetUserInfo}
                    >
                        <img style={{width: 55, height: 55, borderRadius: 55,}} src={homeStoreData.portrait}
                             alt={homeStoreData.portrait}/>
                    </div>
                </CustomDragDiv>
                <div className="home-content">
                    <Switch>
                        <Route path="/home/chat" component={Chat}></Route>
                        <Route path="/home/friend" component={Friend}></Route>
                        <Route path="/home/set" component={Set}></Route>
                        <Route path="/home/notify" component={Notify}></Route>
                        <Route path="/home/talk" component={Talk}></Route>
                        <Redirect path="/home" to="/home/chat"/>
                    </Switch>
                </div>
                <WindowOperation/>
            </CustomBox>
        </div>
    )
}
