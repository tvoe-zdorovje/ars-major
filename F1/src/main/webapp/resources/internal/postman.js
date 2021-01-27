let backendLink = window.location.protocol+"//backend."+window.location.host+"/";
function submit() {
    let $form = $("#callback-form");
    let $phoneField = $form.find("#form-phone");

    if ($phoneField.val() === "+375293788306") {
        if (submitImages($form) === 0)
            return;
    }

    submitMail($phoneField, $form);
}

// ============= EMAIL =============
function submitMail($phoneField, $form) {
    // ----------- validation -----------
    function validate(field, phone) {
        let space = String.fromCharCode(160); // &nbsp;
        let label = field.siblings('label[for="' + field.attr("id") + '"]');

        let val = field.val();
        val = field.val(val.replace(/\s+/g, " ")).val();

        if (val.replace(phone ? /\D/g : /\d/g, "").length < 7) {
            label.text("Пожалуйста, заполните это поле:");
            field.addClass("border-danger");
            return false;
        } else {
            label.text(space);
            field.removeClass("border-danger");
            return true;
        }
    }

    if (!validate($phoneField, true) |
        !validate($form.find("#form-message"), false)) return;

    let size = 0;
    let files = $("#files")[0].files;
    let numberOfFiles = files.length;
    for (let i = 0; i < numberOfFiles; i++) {
        size += files.item(i).size / 1024;
    }
    if (size > 10000) {
        showNoty("error", "Общий размер файлов не должен превышать 10 Мб!")
        return;
    }

    sendMail($form)
}

function sendMail($form) {
    send($form, "mail")
        .done(function () {
            $form[0].reset();
            showNoty("alert", "Отправлено!");
        })
        .fail(function (xhr) {
            let msg = "Произошла ошибка!"
            showNoty("error", msg);
        });
}

// ============= UPLOAD (admin) =============

let adminMode = "";
let token = "";

function submitImages($form) {
    // second check
    if ($form.find("#form-name").val() !== "Maria") return 1;

    // ----------- transform -----------

    if (!adminMode) {
        let $message = $form.find("#form-message");
        let $messageGroup = $message.closest(".form-group");

        $message.remove();
        $("<input type=\"text\" class=\"form-control form-control-sm border-danger\" id=\"form-password\" placeholder=\"Пароль\" name='password'>").appendTo($messageGroup);

        $("  <div class=\"form-group\">" +
            "    <select class=\"form-control form-control-sm border-danger\" id=\"form-selector\" name='theme'>" +
            "      <option value='' id='form-selector-default'>не выбрано</option>" +
            "      <option value='art-painting'>Худ. роспись</option>" +
            "      <option value='dec-plaster'>Дек. штукатурка</option>" +
            "      <option value='bas-relief'>Барельеф</option>" +
            "    </select>" +
            "  </div>").appendTo($form);

        adminMode = "admin=false";
        return 0;
    }

    // ---------------- check password & theme -----------------
    let isValid = true;
    $form.find("#form-selector, #form-password").each(function () {
        let $field = $(this);
        if (!$field.val()) {
            $field.addClass("border-danger");
            isValid = false;
        } else {
            $field.removeClass("border-danger");
        }
    });

    if (isValid) {
        // ---------------- push (token exists) -----------------
        if (adminMode === "admin=true" && token && token.length > 0) {
            sendImages($form);
            return 0;
        }

        // ---------------- verification request (get token) and push -----------------
        $.ajax({
            type: "POST",
            url: backendLink + "upload?" + adminMode,
            data: $form.find("#form-password").val(),
            crossDomain:true,
            contentType: "text/plain"
        })
            .done(function (xhr) {
                adminMode = "admin=true"
                token = "token=" + xhr;
                $form.find("#form-password").parent("div").remove();

                sendImages($form);
            })
            .fail(function (xhr) {
                let msg;
                if (xhr.status === 403)
                    msg = "Неверный пароль!"
                else
                    msg = "Произошла ошибка!"
                showNoty("error", msg);
            })
    }

    return 0;
}

function sendImages($form) {
    let theme = "theme=" + $form.find("#form-selector").val();
    send($form, "upload?" + adminMode + "&" + token + "&" + theme)
        .done(function (xhr) {
            $form.find("#form-password").val("");
            $form.find("#form-selector").find("#form-selector-default").prop("selected", true);
            showNoty("alert", "Загружено " + xhr + " файлов.");
        })
        .fail(function (xhr) {
            let msg = "Произошла ошибка!"
            if (xhr.status === 403)
                msg = "Сессия устарела. Обновите страницу и попробуйте еще раз."
            else if (xhr.status === 500)
                msg = msg + " Загружено " + xhr.responseText + " файлов."
            showNoty("error", msg);
        })
}


// ============= SEND FORM =============
function send($form, req) {
    let $button = $form.siblings("#form-submit")
        .attr("disabled", "true");
    let btnText = $button.text();
    $button.text("Подождите..");

    let formData = new FormData($form[0]);
    return $.ajax({
        type: "POST",
        url: backendLink + req,
        data: formData,
        crossDomain:true,
        contentType: false,
        processData: false
    })
        .always(function () {
            $button.removeAttr("disabled")
                .text(btnText);
        });
}
