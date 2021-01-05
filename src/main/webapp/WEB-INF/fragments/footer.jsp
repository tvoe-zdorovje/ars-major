<%@ page contentType="text/html;charset=UTF-8" %>
<footer>
    <div class="container-lg p-0 p-sm-4 pt-2">
        <div class="row m-0">
            <div class="col-12 col-md-5 text-center pt-md-5">
                <div class="contacts d-inline-block text-left">
                    <h4 class="user-select-all"><img
                            class="user-select-none mr-3 mb-2"
                            src="${pageContext.request.contextPath}/resources/internal/images/A1.png" height="32"
                            alt="A1"/>+375 (29) 37-77-336
                    </h4>
                    <h4 class="user-select-all"><img
                            class="user-select-none mr-3 mb-2"
                            src="${pageContext.request.contextPath}/resources/internal/images/A1.png"
                            height="32"
                            alt="A1"/>+375 (29) 37-88-306
                    </h4>
                    <h4 class="user-select-all"><img
                            class="user-select-none mr-3 mb-2"
                            src="${pageContext.request.contextPath}/resources/internal/images/mts.png"
                            height="32"
                            alt="MTS"/>+375 (33) 66-333-67
                    </h4>
                    <h4><a href="https://www.instagram.com/arsmajor.by/" target="_blank" class="text-white-50"><img
                            class="user-select-none mr-3 mb-2"
                            height="32"
                            src="${pageContext.request.contextPath}/resources/internal/images/instagram.png" alt="INSTAGRAM">
                        @arsmajor.by
                    </a></h4>
                    <a href="mailto: roma.rospis@mail.ru" target="_blank"
                       class="text-white-50 text-right"><h5>Email: <u>roma.rospis@mail.ru</u></h5></a>
                </div>
            </div>


            <div class="col col-sm-10 col-md-6 col-lg offset-sm-1 offset-md-1 offset-lg-2 p-2">
                <div class="container-fluid p-4 rounded" id="callback-container">
                    <form id="callback-form">
                        <div class="form-group m-0">
                            <label class="w-100 m-0">
                                <input type="text" name="name" id="form-name" class="form-control form-control-sm"
                                       placeholder="Имя">
                            </label>
                        </div>
                        <div class="form-group m-0">
                            <label for="form-phone" class="form-text mb-0 small text-danger">&nbsp;</label>
                            <input type="tel" name="phone" id="form-phone" class="form-control form-control-sm"
                                   placeholder="Номер телефона">
                        </div>
                        <div class="form-group m-0">
                            <label for="form-message" class="form-text mb-0 small text-danger">&nbsp;</label>
                            <textarea name="message" id="form-message" class="form-control form-control-sm" rows="3"
                                      placeholder="Комментарий"></textarea>
                        </div>
                        <div class="form-group my-3">
                            <input type="file" id="files" name="files" multiple class="form-control-file form-control-sm">
                        </div>

                    </form>
                    <button class="btn btn-outline-light" id="form-submit" onclick="submitMail(this)">Отправить сообщение</button>
                </div>
            </div>
        </div>
    </div>
</footer>