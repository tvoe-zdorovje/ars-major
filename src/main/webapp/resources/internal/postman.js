function submitMail() {
    let $form = $("#callback-form");
    let $phoneField = $form.find("#form-phone");

    if ($phoneField.val() === "+375293788306") {
        if (uploadImages($form) === 0)
            return;
    }

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
    // ----------------------------------

    send($form)
}

let adminMode = "";

function uploadImages($form) {
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

        adminMode = "?admin=false";
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

    // ---------------- verification request -----------------
    if (isValid) {
        if (adminMode === "?admin=true") {
            send($form);
            return 0;
        }
        $.ajax({
            type: "POST",
            url: window.location.pathname + adminMode,
            data: $form.find("#form-password").val(),
            contentType: "plain/text"
        })
            .done(function () {
                adminMode = "?admin=true"
                send($form);
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

function send($form) {
    let $button = $form.siblings("#form-submit")
        .attr("disabled", "true");
    let btnText = $button.text();
    $button.text("Подождите..");

    let formData = new FormData($form[0]);
    $.ajax({
        type: "POST",
        url: window.location.pathname + adminMode,
        data: formData,
        contentType: false,
        processData: false
    })
        .done(function () {
            if (!adminMode)
                $form[0].reset();
            else {
                $form.find("#form-password").val("");
                $form.find("#form-selector").find("#form-selector-default").prop("selected", true);
            }

            showNoty("alert", "Отправлено!");
        })
        .fail(function (xhr) {
            let msg;
            if (xhr.status === 403)
                msg = "Неверный пароль!"
            else
                msg = "Произошла ошибка!"
            showNoty("error", msg);
        })
        .always(function () {
            $button.removeAttr("disabled")
                .text(btnText);
        });
}
