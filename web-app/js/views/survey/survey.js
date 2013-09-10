$(document).ready(function () {
    function fixBreadcrumbs() {
        function rebindBreadCrumb() {
            $('.breadcrumbButton').each(function(idx, ele) {
                var elementId = $(ele).attr('id').replace(/([ #;&,.+*~\':"!^$[\]()=>|\/])/g, '\\$1');
                $('#'+elementId).unbind('click');
            });
        }
        function repeatUntil(func, delay, count) {
            if (!func()) {
                count = count == null ? 10 : count;
                delay = delay || 100;
                _.delay(function () {
                    repeatUntil(func, delay, --count);
                }, delay);
            }
        }

        repeatUntil(rebindBreadCrumb, 100, 10);
    }
    fixBreadcrumbs();

    var notificationMessages = new Array();
    var error = $.i18n.prop("survey.ethinicity.multiple.selection.invalid");

    $('#chkEthn_1, #chkEthn_2').change(
        function () {
            if ($('#chkEthn_1').is(':checked') &&  $('#chkEthn_2').is(':checked')) {
                $('#ethnicity').addClass("notification-error");
                notificationMessages.push(error);
                if (notificationMessages && notificationMessages.length > 0) {
                    _.each(notificationMessages, function (message) {
                        var n = new Notification({message:message, type:"error"});
                        notifications.addNotification(n);
                    });
                }
            } else {
                notificationMessages.splice(notificationMessages.indexOf(error));
                $('#ethnicity').removeClass("notification-error");
                notifications.clearNotifications();
                notificationCenter.removeNotification();
            }
    });

    $("#save-btn").click(function () {
        if (notificationMessages && notificationMessages.length <= 0) {
            var form = document.getElementById('surveyForm');
            form.submit();
        }
    });


    $("#ask-me-later-btn").click(function () {
        var href = $(this).attr("data-endpoint");
        window.location = href;
    });

});
