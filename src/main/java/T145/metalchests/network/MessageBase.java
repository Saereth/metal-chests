package T145.metalchests.network;

import java.io.IOException;

import com.google.common.base.Throwables;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class MessageBase implements IMessage {

	@Override
	public final void toBytes(ByteBuf buf) {
		serialize(new PacketBuffer(buf));
	}

	@Override
	public final void fromBytes(ByteBuf buf) {
		try {
			deserialize(new PacketBuffer(buf));
		} catch (IOException err) {
			Throwables.throwIfUnchecked(err);
			throw new RuntimeException(err);
		}
	}

	public abstract void serialize(PacketBuffer buf);

	public abstract void deserialize(PacketBuffer buf) throws IOException;

	public abstract void process(MessageContext ctx);
}