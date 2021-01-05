// dec-plaster; art-painting; bas-relief
let CURRENT_THEME = "art-painting";
let CURRENT_THEME_IMGs;

let IMAGE_URLs = {};

$(function () {
    $(".switch").click(switchTo);
    $(".zoomable").click(zoomIn);
    $("#gallery-button").click(openGallery);

    refreshCurrentThemeImages()
    refreshCarousel()
});

function switchTo() {
    let targetId = $(this).attr("id").replace("-card", "");
    const currentActive = $(".description").not("[style*='display: none']");

    if (currentActive.attr("id") === targetId) return;

    $("#description-container").slideUp(function () {
        currentActive.hide();

        $("#top-carousel .carousel-inner").fadeOut("slow", function () {
            $(".carousel-picture").each(function () {
                let $1 = $(this);
                $1.attr("src", $1.attr("src").replaceAll(currentActive.attr("id"), targetId));
            });
        }).fadeIn("slow");

        CURRENT_THEME = targetId;
        refreshCurrentThemeImages();
        refreshCarousel();

        $("#" + targetId).show();
    }).slideDown();
}

function refreshCurrentThemeImages() {
    function loadImages(THEME) {
        let images;
        let URL = window.location.pathname +
            "resources/images/" + THEME + "/";
        $.ajax({
            type: "GET",
            url: URL,
            async: false,
            dataType: "json"
        })
            .done(function (data) {
                images = data;
            })
            .fail(function (a) {
                showNoty("error", "Произошла ошибка при загрузке фото");
                console.log(a)
            });
        return images;
    }

    switch (CURRENT_THEME) {
        case "art-painting":
            CURRENT_THEME_IMGs = !IMAGE_URLs.art_painting ? (IMAGE_URLs.art_painting = loadImages("art-painting")) : IMAGE_URLs.art_painting;
            break;
        case "dec-plaster":
            CURRENT_THEME_IMGs = !IMAGE_URLs.dec_plaster ? (IMAGE_URLs.dec_plaster = loadImages("dec-plaster")) : IMAGE_URLs.dec_plaster;
            break;
        case "bas-relief":
            CURRENT_THEME_IMGs = !IMAGE_URLs.bas_relief ? (IMAGE_URLs.bas_relief = loadImages("bas-relief")) : IMAGE_URLs.bas_relief;
            break;
    }
}

function refreshCarousel() {
    let $gallery = $("#gallery-carousel .carousel-inner");
    let $template = $gallery.children("#template");

    $gallery.find(".carousel-item").remove();
    let size = CURRENT_THEME_IMGs.length > 25 ? 5 : Math.floor(CURRENT_THEME_IMGs.length / 5);
    for (let i = 0, j = 0; i < size; i++) {
        var newItem = $template.clone(true)
            .removeAttr("id")
            .attr("class", "carousel-item")
            .appendTo($gallery);

        newItem.children("img").each(function (idx, value) {
            value.src = CURRENT_THEME_IMGs[j++];
        });
    }
    newItem.addClass("active");
}


function openGallery() {
    let $gallery = $("#gallery");
    let $collection = $gallery.find(".image-collection");

    if ($collection.data("theme") === CURRENT_THEME) {
        $gallery.modal();
        return;
    }

    !$collection.find(".img-container").not(".d-none").remove();
    let $template = $collection.find(".d-none");

    for (let i = 0; i < CURRENT_THEME_IMGs.length; i++) {
        let newItem = $template.clone(true)
            .removeClass("d-none")
            .appendTo($collection);

        newItem.children("img").each(function (idx, img) {
            img.src = CURRENT_THEME_IMGs[i];
        });
    }

    $collection.data("theme", CURRENT_THEME);
    $gallery.modal();
}

function zoomIn() {
    let $viewer = $("#picture-viewer");
    let $carousel = $viewer.find(".carousel-inner");

    $carousel.find(".carousel-item").remove();

    function addItem(img, active) {
        let item = $("<div class='carousel-item " + active + "'></div>").appendTo($carousel);
        img.clone().attr("class", "img-fluid").appendTo(item);
    }

    let $target = $(this);
    let $images = $target.closest(".image-collection").find(".zoomable").not("#template .img-thumbnail");
    $images.each(function () {
        let img = $(this);
        addItem(img, img.is($target) ? "active" : "");
    });

    $viewer.modal();
    setTimeout(function () {
        $viewer.find(".carousel-control-next").focus()
    }, 500);
}

function showNoty(type, msg) {
    new Noty({
        text: msg,
        type: type,
        layout: "bottomRight",
        theme: "bootstrap-v4",
        timeout: 1800
    }).show();
}