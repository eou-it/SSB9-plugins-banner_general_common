/** *****************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
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
    $('#confirmSurvey').hide();
    $('#chkEthn_1').focus();
    var notificationMessages = new Array();
    var error = $.i18n.prop("survey.ethinicity.multiple.selection.invalid");
    var saveBtnLabel = $.i18n.prop("survey.confirm.button.save");
    var cancelBtnLabel = $.i18n.prop("survey.confirm.button.cancel");
    var askMeLaterBtnLabel = $.i18n.prop("survey.edit.button.askMeLater");
    var continueBtnLabel = $.i18n.prop("survey.edit.button.continue");
    var confirmationPageTitle = $.i18n.prop("survey.confirm.title");
    var editPageTitle = $.i18n.prop("survey.edit.title");

    //data
    var nothispanicLabel = $.i18n.prop("survey.ethnicity.nothispanic");
    var hispanicLabel = $.i18n.prop("survey.ethnicity.hispanic");

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

                $('#ethnicity-race-wrapper').append('<div id="error-survey" role="alert" class="hide-aria-message">' + error + '</div>');
                $('#chkEthn_1').attr('aria-invalid', 'true');
                $('#chkEthn_1').attr('aria-describedby', 'error-survey');
                $('#chkEthn_2').attr('aria-invalid', 'true');
                $('#chkEthn_2').attr('aria-describedby', 'error-survey');

            } else {
                notificationMessages.splice(notificationMessages.indexOf(error));
                $('#ethnicity').removeClass("notification-error");
                notifications.clearNotifications();
                notificationCenter.removeNotification();

                $('#chkEthn_1').attr('aria-invalid', 'false');
                $('#chkEthn_2').attr('aria-invalid', 'false');
                // identify and remove error-survey
                $('#chkEthn_1').attr('aria-describedby','');
                $('#chkEthn_2').attr('aria-describedby', '');

                $('#error-survey').remove();
            }
        });

    $("#save-btn").click(function () {
        if ($(this).val() == saveBtnLabel) {
            var form = document.getElementById('surveyForm');
            form.submit();
        } else {
            if (notificationMessages && notificationMessages.length <= 0) {
                $(this).val(saveBtnLabel);
                $("#ask-me-later-btn").val(cancelBtnLabel);
                $('#editSurvey').hide();
                populateConfirmSurvey();
                $('#confirmSurvey').show();
                $('#pagetitle').text(confirmationPageTitle);
            }
        }

    });


    $("#ask-me-later-btn").click(function () {
        if ($(this).val() == askMeLaterBtnLabel) {
            var href = $(this).attr("data-endpoint");
            window.location = href;
        } else {
            $(this).val(askMeLaterBtnLabel);
            $("#save-btn").val(continueBtnLabel);
            $('#editSurvey').show();
            $('#chkEthn_1').focus();
            $('#confirmSurvey').hide();
            $('#pagetitle').text(editPageTitle);
        }

    });

    $('.raceCheckbox:first').focusin(function() {
        $('#race-wrapper').append('<div id="dummy" role="alert" class="hide-aria-message">' + $('#section-header-text2').text() + '</div>');
    });

    $('.raceCheckbox:first').focusout(function() {
        $('#dummy').remove();
    });

    function populateConfirmSurvey() {
        $('#ethinicitytxt').text("");
        if ($('#chkEthn_1').is(':checked')) {
            $('#ethinicitytxt').text(nothispanicLabel)
        } else if ($('#chkEthn_2').is(':checked')) {
            $('#ethinicitytxt').text(hispanicLabel)
        }
        $('#race-content').text("");

        $('div[class="race-category-area"]').each(function (idx, element) {
            var descElement = element;
            var raceElement = element;
            var desc = $(descElement).find('div[class="race-category-header"]').text();
            var raceSelectedDesc = "";

            $(raceElement).find('div[class="races-content"] input:checkbox').each(function (idOfCheckBox, checkBoxElement) {
                if ($(checkBoxElement).is(':checked')) {
                    raceSelectedDesc = raceSelectedDesc+ $("label[for=" + $(checkBoxElement).attr('id') + "]").text()+"<br />";
                }
            });
            if (raceSelectedDesc != "") {
                $('#race-content').append('<div class="row"><div class="column">'+desc+':</div><div class="column">'+ raceSelectedDesc+'</div></div>');
            }

        });
    }

});
