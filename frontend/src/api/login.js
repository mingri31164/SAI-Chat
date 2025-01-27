import Http from '@/utils/axios'

export default {
  login(param) {
    return Http.post('/api/v1/user/login', param)
  },
  register(param) {
    return Http.post('/api/v1/user/register', param)
  },
  getCode(param){
    return Http.get('/api/v1/common/get-code', param)
  },
  logout() {
    return Http.post('/api/v1/user/logout')
  }
}
