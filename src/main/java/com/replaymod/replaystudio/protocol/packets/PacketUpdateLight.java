/*
 * This file is part of ReplayStudio, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 johni0702 <https://github.com/johni0702>
 * Copyright (c) ReplayStudio contributors (see git)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.replaymod.replaystudio.protocol.packets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;

public class PacketUpdateLight {
    @SuppressWarnings("MismatchedReadAndWriteOfArray") // it's supposed to be empty. duh.
    private static final byte[] EMPTY = new byte[2048];
    private int x;
    private int z;
    private List<byte[]> skyLight;
    private List<byte[]> blockLight;

    public static PacketUpdateLight read(Packet packet) throws IOException {
        if (packet.getType() != PacketType.UpdateLight) {
            throw new IllegalArgumentException("Can only read packets of type UpdateLight.");
        }
        PacketUpdateLight updateLight = new PacketUpdateLight();
        try (Packet.Reader reader = packet.reader()) {
            updateLight.read(packet, reader);
        }
        return updateLight;
    }

    public Packet write(PacketTypeRegistry registry) throws IOException {
        Packet packet = new Packet(registry, PacketType.UpdateLight);
        try (Packet.Writer writer = packet.overwrite()) {
            write(packet, writer);
        }
        return packet;
    }

    private PacketUpdateLight() {
    }

    public PacketUpdateLight(int x, int z, List<byte[]> skyLight, List<byte[]> blockLight) {
        if (skyLight.size() != 18) {
            throw new IllegalArgumentException("skyLight must have exactly 18 entries (null entries are permitted)");
        }
        if (blockLight.size() != 18) {
            throw new IllegalArgumentException("blockLight must have exactly 18 entries (null entries are permitted)");
        }
        this.x = x;
        this.z = z;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public List<byte[]> getSkyLight() {
        return this.skyLight;
    }

    public List<byte[]> getBlockLight() {
        return this.blockLight;
    }

    private void read(Packet packet, NetInput in) throws IOException {
        this.x = in.readVarInt();
        this.z = in.readVarInt();
        if (packet.atLeast(ProtocolVersion.v1_16)) {
            in.readBoolean(); // unknown
        }

        int skyLightMask = in.readVarInt();
        int blockLightMask = in.readVarInt();
        int emptySkyLightMask = in.readVarInt();
        int emptyBlockLightMask = in.readVarInt();

        this.skyLight = new ArrayList<>(18);
        for (int i = 0; i < 18; i++) {
            if ((skyLightMask & 1 << i) != 0) {
                if (in.readVarInt() != 2048) {
                    throw new IOException("Expected sky light byte array to be of length 2048");
                }
                this.skyLight.add(in.readBytes(2048)); // 2048 bytes read = 4096 entries
            } else if ((emptySkyLightMask & 1 << i) != 0) {
                this.skyLight.add(new byte[2048]);
            } else {
                this.skyLight.add(null);
            }
        }

        this.blockLight = new ArrayList<>(18);
        for (int i = 0; i < 18; i++) {
            if ((blockLightMask & 1 << i) != 0) {
                if (in.readVarInt() != 2048) {
                    throw new IOException("Expected block light byte array to be of length 2048");
                }
                this.blockLight.add(in.readBytes(2048)); // 2048 bytes read = 4096 entries
            } else if ((emptyBlockLightMask & 1 << i) != 0) {
                this.blockLight.add(new byte[2048]);
            } else {
                this.blockLight.add(null);
            }
        }
    }

    private void write(Packet packet, NetOutput out) throws IOException {
        out.writeVarInt(this.x);
        out.writeVarInt(this.z);
        if (packet.atLeast(ProtocolVersion.v1_16)) {
            out.writeBoolean(true); // unknown, ViaVersion always writes true, so we'll do so as well
        }

        int skyLightMask = 0;
        int blockLightMask = 0;
        int emptySkyLightMask = 0;
        int emptyBlockLightMask = 0;

        for (int i = 0; i < 18; i++) {
            byte[] skyLight = this.skyLight.get(i);
            if (skyLight != null) {
                if (Arrays.equals(EMPTY, skyLight)) {
                    emptySkyLightMask |= 1 << i;
                } else {
                    skyLightMask |= 1 << i;
                }
            }
            byte[] blockLight = this.blockLight.get(i);
            if (blockLight != null) {
                if (Arrays.equals(EMPTY, blockLight)) {
                    emptyBlockLightMask |= 1 << i;
                } else {
                    blockLightMask |= 1 << i;
                }
            }
        }

        out.writeVarInt(skyLightMask);
        out.writeVarInt(blockLightMask);
        out.writeVarInt(emptySkyLightMask);
        out.writeVarInt(emptyBlockLightMask);

        for (int i = 0; i < 18; i++) {
            if ((skyLightMask & 1 << i) != 0) {
                out.writeVarInt(2048); // dunno why Minecraft feels the need to send these
                out.writeBytes(this.skyLight.get(i));
            }
        }

        for (int i = 0; i < 18; i++) {
            if ((blockLightMask & 1 << i) != 0) {
                out.writeVarInt(2048); // dunno why Minecraft feels the need to send these
                out.writeBytes(this.blockLight.get(i));
            }
        }
    }
}
