package com.sean.backpackininventory.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModPayloads {
    private ModPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("2");
        registrar.playToServer(OpenStoragePayload.TYPE, OpenStoragePayload.STREAM_CODEC, OpenStoragePayload::handle);
        registrar.playToServer(OpenInventoryPayload.TYPE, OpenInventoryPayload.STREAM_CODEC, OpenInventoryPayload::handle);
        registrar.playToServer(SelectBackpackPayload.TYPE, SelectBackpackPayload.STREAM_CODEC, SelectBackpackPayload::handle);
        registrar.playToServer(PlaceRecipePayload.TYPE, PlaceRecipePayload.STREAM_CODEC, PlaceRecipePayload::handle);
        registrar.playToClient(OpenVanillaPayload.TYPE, OpenVanillaPayload.STREAM_CODEC, OpenVanillaPayload::handle);
    }
}
