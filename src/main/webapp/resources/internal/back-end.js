function submitMail(button) {
    let $form = $("#callback-form");

    // ----------- validation -----------
    function validate(field, phone) {
        let space = String.fromCharCode(160); // &nbsp;
        let label = field.siblings('label[for="' + field.attr("id") + '"]');

        let val = field.val();
        val = field.val(val.replace(/\s+/g, " ")).val();

        if (val.replace(phone ? /\D/g : /\W/g, "").length < 7) {
            label.text("Пожалуйста, заполните это поле:");
            field.addClass("border-danger");
            return false;
        } else {
            label.text(space);
            field.removeClass("border-danger");
            return true;
        }
    }

    if (!validate($form.find("#form-phone", true)) |
        !validate($form.find("#form-message", false))) return;
    // ----------------------------------

    let $button = $(button)
        .attr("disabled", "true");
    let btnText = $button.text();
    $button.text("Подождите..");

    let formData = new FormData($form[0]);
    $.ajax({
        type: "POST",
        url: window.location.pathname,
        data: formData,
        contentType: false,
        processData: false
    })
        .done(function () {
            $form[0].reset();
            showNoty("alert", "Отправлено!");
        })
        .fail(function () {
            showNoty("error", "Произошла ошибка!")
        })
        .always(function () {
            $button.removeAttr("disabled")
                .text(btnText);
        });
}
