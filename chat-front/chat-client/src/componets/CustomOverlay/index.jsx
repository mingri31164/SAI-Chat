import "./index.less"
import {useEffect, useRef, useState} from "react";
import {listen} from "@tauri-apps/api/event";

export default function CustomOverlay({visible, position, width, children, onClose}) {
    const [inVisible, setInVisible] = useState(visible)
    const contentRef = useRef(null);

    useEffect(() => {
        setInVisible(visible)
    }, [visible])

    useEffect(() => {
        if (contentRef.current) {
            contentRef.current.focus()
        }
    }, [inVisible])

    useEffect(() => {
        const unListen = listen('drag-click', (event) => {
            setInVisible(false)
            if (onClose) onClose()
        });
        return async () => {
            (await unListen)()
        }
    }, [])

    return (
        <div onMouseDown={(e) => {
            e.stopPropagation()
        }}>
            <div className="custom-overlay">
                {inVisible &&
                    <div
                        ref={contentRef}
                        className="content"
                        style={{top: position.x, left: position.y, width: width, outline: "none"}}
                        tabIndex="0"
                        onBlur={() => {
                            setInVisible(false);
                            if (onClose) onClose()
                        }}
                    >
                        {children}
                    </div>
                }
            </div>
        </div>
    )
}