// dec-plaster; art-painting; bas-relief
let THEMES = {};
let CURRENT_THEME;

$(function () {
    init();

    $(".switch").click(switchTo);
    $(".zoomable").click(zoomIn);
    $("#gallery-button").click(openGallery);
});

function init() {
/* links {"carousel": [], "gallery" = []} */
    function loadImageLinks(id) {
        let links;
        let URL = window.location.pathname +
            "resources/images/" + id + "/";
        $.ajax({
            type: "GET",
            url: URL,
            async: false,
            dataType: "json"
        })
            .done(function (data) {
                links = data;
            })
            .fail(function () {
                showNoty("error", "Произошла ошибка при загрузке фото");
            });
        return links;
    }

    // ------ create themes ------
    let art_painting = {};
    art_painting.id = "art-painting";
    let links = loadImageLinks(art_painting.id);
    art_painting.gallery = links.gallery;
    art_painting.carousel = links.carousel;

    let dec_plaster = {};
    dec_plaster.id = "dec-plaster";
    links = loadImageLinks(dec_plaster.id);
    dec_plaster.gallery = links.gallery;
    dec_plaster.carousel = links.carousel;

    let bas_relief = {};
    bas_relief.id = "bas-relief";
    links = loadImageLinks(bas_relief.id);
    bas_relief.gallery = links.gallery;
    bas_relief.carousel = links.carousel;

    THEMES.art_painting = art_painting;
    THEMES.dec_plaster = dec_plaster;
    THEMES.bas_relief = bas_relief;

    // ------ set card images ------
    $("#art-painting-card img").attr("src", art_painting.carousel[0]);
    $("#dec-plaster-card img").attr("src", dec_plaster.carousel[0]);
    $("#bas-relief-card img").attr("src", bas_relief.carousel[0]);

    // ------ set theme ------
    CURRENT_THEME = THEMES.art_painting;
    refreshTopCarousel();
    refreshGalleryCarousel();
}

function refreshTopCarousel() {
    let $carousel_inner = $("#top-carousel .carousel-inner");
    let $template = $carousel_inner.children(".template");

    $carousel_inner.find(".carousel-item").remove();
    let size = CURRENT_THEME.carousel.length;
    for (let i = 0; i < size; i++) {
        var newItem = $template.clone(true)
            .attr("class", "carousel-item")
            .appendTo($carousel_inner);

        newItem.children("img.carousel-picture").each(function (idx, value) {
            value.src = CURRENT_THEME.carousel[i++];
        });
    }
    newItem.addClass("active");
}

function refreshGalleryCarousel() {
    let $carousel_inner = $("#gallery-carousel .carousel-inner");
    let $template = $carousel_inner.children("#template");

    $carousel_inner.find(".carousel-item").remove();
    let numberOfImages = CURRENT_THEME.gallery.length;
    let size = numberOfImages > 25 ? 5 : Math.floor(numberOfImages / 5);
    for (let i = 0, j = 0; i < size; i++) {
        var newItem = $template.clone(true)
            .removeAttr("id")
            .attr("class", "carousel-item")
            .appendTo($carousel_inner);

        newItem.children("img").each(function (idx, value) {
            value.src = CURRENT_THEME.gallery[j++];
        });
    }
    newItem.addClass("active");
}

function switchTo() {
    let targetId = $(this).attr("id").replace("-card", "");
    const currentActive = $(".description").not("[style*='display: none']");

    if (currentActive.attr("id") === targetId) return;

    switch (targetId) {
        case THEMES.art_painting.id:
            CURRENT_THEME = THEMES.art_painting;
            break;
        case THEMES.dec_plaster.id:
            CURRENT_THEME = THEMES.dec_plaster;
            break;
        case THEMES.bas_relief.id:
            CURRENT_THEME = THEMES.bas_relief;
            break;
    }

    $("#description-container").slideUp(function () {
        currentActive.hide();

        $("#top-carousel .carousel-inner").fadeOut("slow", function () {
            refreshTopCarousel();
        }).fadeIn("slow");

        refreshGalleryCarousel();

        $("#" + targetId).show();
    }).slideDown();
}

function openGallery() {
    let $gallery = $("#gallery");
    let $collection = $gallery.find(".image-collection");

    if ($collection.data("theme") === CURRENT_THEME.id) {
        $gallery.modal();
        return;
    }

    !$collection.find(".img-container").not(".d-none").remove();
    let $template = $collection.find(".d-none");

    for (let i = 0; i < CURRENT_THEME.gallery.length; i++) {
        let newItem = $template.clone(true)
            .removeClass("d-none")
            .appendTo($collection);

        newItem.children("img").each(function (idx, img) {
            img.src = CURRENT_THEME.gallery[i];
        });
    }

    $collection.data("theme", CURRENT_THEME.id);
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