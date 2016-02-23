package messages.exchange;

import utilities.ByteArray;
import utilities.Int64;

public class ExchangePlayerRequestMessage extends ExchangeRequestMessage {
	public double target;

	public ExchangePlayerRequestMessage() {
		super();
	}

	public void serialize(double target, int exchangeType) {
		ByteArray buffer = new ByteArray();
		super.serialize(buffer, exchangeType);
		buffer.writeVarLong(Int64.fromNumber(target));
		completeInfos(buffer);
	}
}