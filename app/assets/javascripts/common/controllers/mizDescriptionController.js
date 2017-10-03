define(['angular', 'jquery', '../services/mizService'], function (angular, jQuery) {
    'use strict';
    var mod = angular.module('common.controllers.mizDescription', ['common.services.miz']);
    mod.controller('MizDescriptionCtrl', ['$scope', 'mizService', '$window', '$sce', '$anchorScroll', '$location',
        function ($scope, mizService, $window, $sce, $anchorScroll, $location) {
            var editor;
            $scope.init = function () {
                if (!$window.readOnly) {
                    $scope.isEditting = false;
                    $scope.isLoading = false;
                    $scope.miz = {};
                    $scope.$watch(function () {
                        return $scope.$parent.miz;
                    }, function(miz) {
                        if (miz && !$scope.form.description.$dirty) {
                            $scope.miz.contentBody = miz.contentBody;
                            $scope.contentBodyHtml = $sce.trustAsHtml(miz.contentBody);
                        }
                    }, true);

                    $scope.$watch(function () {
                        return $scope.miz.contentBody;
                    }, function(contentBody) {
                        if (contentBody) {
                            $scope.contentBodyHtml = $sce.trustAsHtml(contentBody);
                        }
                    }, true);

                    $scope.tinyMceOptions = {
                        'content_css' : '/assets/css/style.css',
                        'height' : 300,
                        'plugins' : 'image',
                        'image_description': false,
                        'image_dimensions': false,
                        'statusbar': false,
                        'menubar': false,
                        'toolbar': "undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | table | fontsizeselect | myimage",
                        'setup': function(ed) {
                            // Add a custom button
                            ed.addButton('myimage', {
                                'title' : 'Insert Image',
                                'icon': 'image',
                                'onclick' : function() {
                                    editor = ed;
                                    jQuery('#description-photo').find('.fileInput').click();
                                }
                            });
                        }
                    };
                }

                angular.element(window).on('beforeunload', function () {
                    if ($scope.isEditting) {
                        $location.hash('mizDescription');
                        $anchorScroll();
                        return 'You are currently editing a form.';
                    }
                });
                $scope.readOnly =  !!$window.readOnly;
            };

            $scope.editorInsertPicture = function () {
                editor.insertContent('<img src="' + $scope.descriptionPic + '" />');
            };

            $scope.cancel = function () {
                var miz = $scope.$parent.miz;
                $scope.miz.contentBody = miz.contentBody;
                $scope.isLoading = false;
            };

            $scope.submit = function () {
                if ($scope.form.$invalid) {
                    return;
                }

                if ($scope.form.description.$dirty) {
                    $scope.isLoading = true;
                    mizService.updateMiz(window.mizTitle, {'contentBody': $scope.miz.contentBody}).then(function (patchedMiz) {
                        $scope.isLoading = false;
                        $scope.isEditting = false;
                        $scope.miz.contentBody = patchedMiz.contentBody;
                        $scope.$parent.miz = patchedMiz;
                    });
                } else {
                    $scope.isLoading = false;
                }
            };

            $scope.init();
    }]);
    return mod;
});