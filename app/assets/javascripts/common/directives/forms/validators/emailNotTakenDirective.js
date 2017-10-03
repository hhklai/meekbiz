define(['angular', '../../../services/userService'], function (angular) {
    'use strict';
    var mod = angular.module('common.directives.forms.validators.emailNotTaken', ["common.services.user"]);
    mod.directive('emailNotTaken', ['userService', function (userService) {
        return {
            require: 'ngModel',
            scope: {
                emailNotTaken: '='
            },
            restrict: 'A',
            link: function (scope, elm, attrs, ctrl) {
                ctrl.$parsers.push(function (viewValue) {
                    if (!viewValue) {
                        ctrl.$setValidity('untaken', true);  //reset it
                        return null;
                    }
                    //check for ignore string
                    if (!!scope.emailNotTaken && scope.emailNotTaken.length > 0) {
                        if (scope.emailNotTaken === viewValue) {
                            ctrl.$setValidity('untaken', true);
                            return viewValue;
                        };
                    }
                    userService.checkIfEmailExists(viewValue).then(function (response) {
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