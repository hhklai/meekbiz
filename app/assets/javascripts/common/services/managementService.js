define(['angular', '../lib/playRoutes'], function (angular) {
    'use strict';
    var mod = angular.module('common.services.management', ['common.lib.playRoutes']);
    mod.service('managementService', ['playRoutes', '$q', '$http',
        function (playRoutes) {

        var self = this;

        self.reInitSearch = function () {
            return playRoutes.api.controllers.SearchController.reInitSearch().put();
        }

        self.synchWithNewMizes = function () {
            return playRoutes.api.controllers.SearchController.synchSearchIndexWithNewMizes().put();
        };

        self.rebuildSearchIndex = function () {
            return playRoutes.api.controllers.SearchController.rebuildSearchIndex().put();
        }

    }]);
    return mod;
});