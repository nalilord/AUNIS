package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class NaquadahGeneratorRendererState extends State {

    public boolean isActive;
    public int powerBuffer;
    public int energyStored;
    public int fluidStored;

    public NaquadahGeneratorRendererState() {
        this(false, 0, 0, 0);
    }

    public NaquadahGeneratorRendererState(boolean isActive, int powerBuffer, int energyStored, int fluidStored) {
        this.isActive = isActive;
        this.powerBuffer = powerBuffer;
        this.energyStored = energyStored;
        this.fluidStored = fluidStored;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isActive);
        buf.writeInt(powerBuffer);
        buf.writeInt(energyStored);
        buf.writeInt(fluidStored);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isActive = buf.readBoolean();
        this.powerBuffer = buf.readInt();
        this.energyStored = buf.readInt();
        this.fluidStored = buf.readInt();
    }
}
