define(['angular', 'jquery', 'ui-bootstrap', 'ui-bootstrap-tpls', '../thirdpartylibs/ng-img-crop'], function (angular, jQuery) {
    'use strict';
    var mod = angular.module('common.directives.photoInput', ['ui.bootstrap', 'ui.bootstrap.tpls', 'common.thirdparty.ngImgCrop']);
    mod.directive('photoInput', ['$modal' , function ($modal) {

        var fileInputController = function ($scope, $modalInstance) {
            $scope.myCroppedImage = '';
            $scope.ok = function (myCroppedImage) {
                $scope.$parent.thumbnail = myCroppedImage;
                if ($scope.onChange) {
                    setTimeout(function () {
                        $scope.onChange();
                    }); //wait for scope.$apply to finish
                }
                $modalInstance.close();
            }
        };
        fileInputController.$inject = ['$scope', '$modalInstance'];

        jQuery.event.props.push('dataTransfer');
        return {
            restrict: 'E',
            scope: {
                isEditting: '=',
                thumbnail: '=',
                hasChanged: '=',
                resultSize: '=',
                aspectRatio: '=',
                onChange: '&'
            },
            template: '<input style="position:fixed;top:-100px" type="file" accept="image/*" class="fileInput" />' +
                '<div style="position:relative;display:inline-block">' +
                    '<a ng-click="onClickThumbnail()">' +
                        '<img style="max-width:100%" ng-src="{{thumbnail}}" />' +
                        '<span class="overlay" ng-show="isEditting" style="width: 100%;height: 100%; position: absolute;top: 0;left: 0;cursor: pointer;color: #ffffff;background-color: rgba(0, 0, 0, 0.5);">' +
                            '<div class="fileDropZone" style="position:relative;height:100%">' +
                                '<i class="icon-edit icon-3x" style="position:absolute;top:50%;left:50%;margin-left:-15px;margin-top:-30px;"></i>' +
                                '<span style="position:absolute;top:50%;left:50%;margin-left:-37px;margin-top:20px">Click To Edit</span>' +
                            '</div>' +
                        '</span>' +
                    '</a>' +
                '</div>',
            link: function ($scope, element) {
                $scope.onClickThumbnail = function () {
                    if ($scope.isEditting) {
                        setTimeout(function () {
                            jQuery(element).find('.fileInput').click();
                        }); //wait for scope.$apply to finish
                    }
                };

                $scope.init = function () {
                    $scope.myImage='';

                    var openModal = function () {
                        $modal.open({
                            template: '<div>' +
                                '<div class="modal-body">' +
                                '<div class="cropArea" style="height:300px">' +
                                '<img-crop image="myImage" result-image="myCroppedImage" area-type="rectangle" aspect-ratio="aspectRatio" result-image-size="resultSize"></img-crop>' +
                                '</div>' +
                                '<div class="hide"><img ng-src="{{myCroppedImage}}" /></div>' +
                                '</div>' +
                                '<div class="modal-footer">' +
                                '<button class="btn btn-primary" ng-click="ok(myCroppedImage)">Select</button>' +
                                '</div>' +
                                '</div>',
                            scope: $scope,
                            backdrop: 'static',
                            controller: fileInputController
                        });
                    };

                    var handleFileSelect=function(evt) {
                        if (!$scope.isEditting) {
                            return;
                        }
                        if (evt.currentTarget.files.length < 1) {
                            return;
                        }
                        if (evt.currentTarget.files[0].type.indexOf('image') !== 0) {
                            return;
                        }
                        openModal();

                        var file=evt.currentTarget.files[0];
                        var reader = new FileReader();
                        reader.onload = function (evt) {
                            $scope.$apply(function($scope){
                                $scope.myImage=evt.target.result;
                                $scope.hasChanged = true;
                            });
                            jQuery(element).find('.fileInput').val('');
                        };

                        reader.readAsDataURL(file);
                    };
                    var jElem = jQuery(element);
                    jElem.find('.fileInput').on('change',handleFileSelect);

                    jElem.find('.fileDropZone')
                        .bind( 'dragenter dragover', false)
                        .bind( 'drop', function( e ) {
                            e.stopPropagation();
                            e.preventDefault();

                            e.currentTarget = e.dataTransfer;
                            handleFileSelect(e);
                        });
                };

                $scope.init();
            }
        };
    }]);
    return mod;
});