import Http from "../utils/api";

export default {
    search(param) {
        return Http.post(`/v1/api/user/search`, param)
    },
    unread() {
        return Http.get(`/v1/api/user/unread`)
    },
    info() {
        return Http.get(`/v1/api/user/info`)
    },
    upload(param) {
        return Http.upload(`/v1/api/user/upload/portrait`, param)
    },
    update(param) {
        return Http.post(`/v1/api/user/update`, param)
    },
    updatePassword(param) {
        return Http.post(`/v1/api/user/update/password`, param)
    },
    getImg(param) {
        return Http.get("/v1/api/user/get/img", param)
    },
    register(param) {
        return Http.post("/v1/api/user/register", param)
    },
    forget(param) {
        return Http.post("/v1/api/user/forget", param)
    },
    emailVerification(param) {
        return Http.post("/v1/api/user/email/verify", param)
    },
};
