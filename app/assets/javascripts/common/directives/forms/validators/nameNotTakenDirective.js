define(['angular', '../../../services/userService'], function (angular) {
    'use strict';
    var mod = angular.module('common.directives.forms.validators.nameNotTaken', ["common.services.user"]);
    mod.directive('nameNotTaken', ['userService', function (userService) {
        return {
            require: 'ngModel',
            restrict: 'A',
            scope: {
                nameNotTaken: '='
            },
            link: function (scope, elm, attrs, ctrl) {
                ctrl.$parsers.push(function (viewValue) {
                    if (!viewValue) {
                        ctrl.$setValidity('untaken', true);  //reset it
                        return null;
                    }
                    //check for ignore string
                    if (!!scope.nameNotTaken && scope.nameNotTaken.length > 0) {
                        if (scope.nameNotTaken === viewValue) {
                            ctrl.$setValidity('untaken', true);
                            return viewValue;
                        };
                    }
                    userService.checkIfNameExists(viewValue).then(function (response) {
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