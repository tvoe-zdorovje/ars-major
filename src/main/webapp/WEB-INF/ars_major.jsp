<%--<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>--%>
<%@ page contentType="text/html;charset=UTF-8"%>
<!doctype html>
<html lang="ru">
<jsp:include page="/WEB-INF/fragments/headTag.jsp"/>
<body>
<div class="container-fluid" id="body">

    <%--<jsp:include page="/WEB-INF/fragments/header.jsp"/>--%>
    <jsp:include page="/WEB-INF/fragments/gallery.jsp"/>
    <jsp:include page="/WEB-INF/fragments/picViewer.jsp"/>

    <div class="container-fluid p-0">
        <div id="top-carousel" class="carousel slide carousel-fade user-select-none" data-ride="carousel"
             data-interval="10000">
            <!-- CARDS -->
            <div class="carousel-indicators">
                <div class="container-fluid position-absolute text-center p-0"
                     style="top: 50%; left: 0; right: 0; transform: translateY(-50%);">
                    <div class="row">
                        <div class="col p-0 card bg-dark text-white shadow-lg active switch" role="button"
                             id="art-painting-card"
                             style="width: 18rem;">
                            <img src="${pageContext.request.contextPath}/resources/images/art-painting/carousel-1.jpg"
                                 class="card-img darkened-pic-80"
                                 alt="art-painting button">
                            <div class="card-img-overlay p-0 d-flex align-items-center">
                                <p class="card-title">Художественная роспись</p>
                            </div>
                        </div>
                        <div class="col p-0 card bg-dark text-white shadow-lg switch" role="button"
                             id="dec-plaster-card"
                             style="width: 18rem;">
                            <img src="${pageContext.request.contextPath}/resources/images/dec-plaster/carousel-1.jpg"
                                 class="card-img darkened-pic-80"
                                 alt="dec-plaster button">
                            <div class="card-img-overlay p-0 d-flex align-items-center">
                                <p class="card-title">Декоративная Штукатурка</p>
                            </div>
                        </div>
                        <div class="col p-0 card bg-dark text-white shadow-lg switch" role="button" id="bas-relief-card"
                             style="width: 18rem;">
                            <img src="${pageContext.request.contextPath}/resources/images/bas-relief/carousel-1.jpg"
                                 class="card-img darkened-pic-80"
                                 alt="bas-relief button">
                            <div class="card-img-overlay p-0 d-flex align-items-center">
                                <p class="card-title">Барельеф</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- IMAGES -->
            <div class="carousel-inner border border-top-0 border-dark shadow-lg">
                <div class="carousel-item active">
                    <img src="${pageContext.request.contextPath}/resources/images/art-painting/carousel-1.jpg"
                         class="d-block w-100 carousel-picture" alt="фото">
                    <div class="position-absolute text-center logo">
                        <img src="${pageContext.request.contextPath}/resources/internal/images/logo.png" class="d-block w-100" alt="Ars Major">
                    </div>
                </div>
                <div class="carousel-item">
                    <img src="${pageContext.request.contextPath}/resources/images/art-painting/carousel-2.jpg"
                         class="d-block w-100 carousel-picture" alt="фото">
                    <div class="position-absolute text-center logo">
                        <img src="${pageContext.request.contextPath}/resources/internal/images/logo.png" class="d-block w-100" alt="Ars Major">
                    </div>
                </div>
                <div class="carousel-item">
                    <img src="${pageContext.request.contextPath}/resources/images/art-painting/carousel-3.jpg"
                         class="d-block w-100 carousel-picture" alt="фото">
                    <div class="position-absolute text-center logo">
                        <img src="${pageContext.request.contextPath}/resources/internal/images/logo.png" class="d-block w-100" alt="Ars Major">
                    </div>
                </div>
            </div>
        </div>
    </div>


    <div class="container" id="description-container">
        <!--    <br><br><br><br><br><br>-->
        <div id="art-painting" class="description">
            <h2 class="description-title text-center">Художественная роспись</h2>
            <div class="description-body mt-3 mt-md-4 mt-xl-5">
                <p class="text-justify"> Лучший способ сделать ваш интерьер особенным – украсить его художественной
                    росписью. Даже супердорогие обои с «неповторимым» рисунком, сделанным под заказ, несравнимы с
                    творением
                    рук человеческих – настенной росписью.
                </p>
                <p class="text-justify"> Во все времена художественные произведения украшали дворцы, храмы, дома богатых
                    людей. Они являлись роскошью, но теперь пришло время, когда роскошь стала доступной для всех!
                </p>
                <p class="text-justify"> Большую часть жизни мы проводим в своем доме и, конечно же, хотели бы видеть
                    его
                    особенным и неповторимым. Для этого необходимо что-то эксклюзивное, не отштампованное на станке.
                    Такой
                    находкой и являются наши настенные рисунки.
                    Кисть художника превратит ваш дом во что-то сказочно-неповторимое, и поэтому вам
                    нужна наша роспись! Наши услуги необходимы вам! Если вы не хотите походить на других, смешаться с
                    общей
                    массой людей – звоните!
                    Мы мастерски украсим ваше жилье настенной живописью любого направления, исходя из
                    ваших пожеланий. Также мы поможем вам в декорировании интерьера (дизайн-этюд).
                </p>
            </div>
        </div>
        <div id="dec-plaster" class="description" style="display: none">
            <h2 class="description-title text-center">Декоративная штукатурка</h2>
            <div class="description-body mt-3 mt-md-4 mt-xl-5">
                <p class="text-justify">Декоративная штукатурка относится к универсальным отделочным материалам: стены,
                    оформленные этим составом,
                    подойдут и для гостиной, и для спальни, а также для общественных заведений (магазинов, ресторанов,
                    кафе).
                    Рельефная поверхность стен, созданная при помощи штукатурки, придает помещению уникальный колорит.
                    Современная
                    декоративная штукатурка – достаточно прочный и устойчивый к механическому воздействию материал. Ее
                    можно
                    мыть
                    любыми чистящими средствами, при этом она не потеряет начальной цветовой насыщенности.
                </p>
                <p class="text-justify">
                    Декоративная штукатурка обладает хорошими тепло- и влагоизоляционными свойствами, устойчивостью к
                    ультрафиолетовому излучению.
                </p>
                <p class="text-justify">
                    Рабочими основаниями для декоративной штукатурки служат кирпич, все типы бетона, ДСП, цементная
                    штукатурка,
                    гипсокартон, армирующий слоя в системе утепления. Группа декоративных штукатурок делится на виды в
                    соответствии
                    с материалом основы (минеральные, силикатные, синтетические, водные), со степенью зернистости, с
                    рельефом.
                    Цена
                    на них отличается в зависимости от сложности нанесения.
                </p>
            </div>
        </div>
        <div id="bas-relief" class="description" style="display: none">
            <h2 class="description-title text-center">Барельеф</h2>
            <div class="description-body mt-3 mt-md-4 mt-xl-5">
                <p class="text-justify"> Барельефы представляют собой выступающую на плоскости скульптуру или
                    изображение.
                    Также иногда называют барельефы объемным художественными панно.
                </p>
                <p class="text-justify"> Художественное панно в оформлении современного жилища выглядит очень изящно и
                    роскошно, демонстрируя превосходный вкус хозяина. Всегда можно заказать барельефы ручной работы,
                    аналогов которых просто не существует.
                </p>
                <p class="text-justify"> Барельеф – это замечательный предмет декора, способный дополнить композицию
                    вашего
                    интерьера, придать уникальность и оживить пустующую стену.
                </p>
            </div>
        </div>

        <div id="gallery-carousel" class="carousel slide mt-5 mb-3" data-ride="carousel" data-interval="false">
            <div class="carousel-inner text-center image-collection">
                <div id="template" class="d-none">
                    <img src=""
                         class="img-thumbnail zoomable" alt="фото">
                    <img src=""
                         class="img-thumbnail zoomable d-none d-sm-inline" alt="фото">
                    <img src=""
                         class="img-thumbnail zoomable d-none d-md-inline" alt="фото">
                    <img src=""
                         class="img-thumbnail zoomable d-none d-lg-inline" alt="фото">
                    <img src=""
                         class="img-thumbnail zoomable d-none d-xl-inline" alt="фото">
                </div>
            </div>
            <a class="carousel-control-prev invert" href="#gallery-carousel" role="button" data-slide="prev">
                <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                <span class="sr-only">Previous</span>
            </a>
            <a class="carousel-control-next invert" href="#gallery-carousel" role="button" data-slide="next">
                <span class="carousel-control-next-icon" aria-hidden="true"></span>
                <span class="sr-only">Next</span>
            </a>
        </div>

        <div class="text-center pb-4">
            <button id="gallery-button" role="button" class="btn btn-outline-secondary mr-auto ml-auto">Галерея</button>
        </div>
    </div>
    <jsp:include page="/WEB-INF/fragments/footer.jsp"/>

</div>
</body>
</html>