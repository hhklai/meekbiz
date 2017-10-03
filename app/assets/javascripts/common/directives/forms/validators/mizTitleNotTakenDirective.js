define(['angular', '../../../services/mizService'], function (angular) {
    'use strict';
    var mod = angular.module('common.directives.forms.validators.mizTitleNotTaken', ["common.services.miz"]);
    mod.directive('mizTitleNotTaken', ['mizService', function (mizService) {
        return {
            require: 'ngModel',
            scope: {
                mizTitleNotTaken: '='
            },
            restrict: 'A',
            link: function (scope, elm, attrs, ctrl) {
                ctrl.$parsers.push(function (viewValue) {
                    if (!viewValue) {
                        ctrl.$setValidity('untaken', true);  //reset it
                        return null;
                    }
                    //check for ignore string
                    if (!!scope.mizTitleNotTaken && scope.mizTitleNotTaken.length > 0) {
                        if (scope.mizTitleNotTaken === viewValue) {
                            ctrl.$setValidity('untaken', true);
                            return viewValue;
                        };
                    }
                    mizService.checkIfMizExists(viewValue).then(function (response) {
                        if (response.data.found) {
                            ctrl.$setValidity('untaken', false);
                        } else {
                            ctrl.$setValidity('untaken', true);
                        }
                    });
                    ctrl.$setValidity('untaken', true);
                    return viewValue;
                });
            }
        };
    }]);
    return mod;
});