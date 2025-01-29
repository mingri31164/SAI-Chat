import Http from '@/utils/axios'

export default {
  offer(param) {
    return Http.post(`/api/v1/call/offer`, param)
  },
  answer(param) {
    return Http.post(`/api/v1/call/answer`, param)
  },
  candidate(param) {
    return Http.post(`/api/v1/call/candidate`, param)
  },
  hangup(param) {
    return Http.post(`/api/v1/call/hangup`, param)
  },
  invite(param) {
    return Http.post(`/api/v1/call/invite`, param)
  },
  accept(param) {
    return Http.post(`/api/v1/call/accept`, param)
  },
}
