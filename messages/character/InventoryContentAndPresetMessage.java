package messages.character;

import gamedata.inventory.IdolsPreset;
import gamedata.inventory.ObjectItem;
import gamedata.inventory.Preset;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class InventoryContentAndPresetMessage extends Message {
	public Vector<ObjectItem> inventory;
	public int kamas;
	public Vector<Preset> presets;
	public Vector<IdolsPreset> idolsPresets;
	
	public InventoryContentAndPresetMessage(Message msg) {
		super(msg);
		inventory = new Vector<ObjectItem>();
		presets = new Vector<Preset>();
		idolsPresets = new Vector<IdolsPreset>();
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		deserializeInventory(buffer);
		deserializePresets(buffer);
	}

	private void deserializeInventory(ByteArray buffer){
		ObjectItem obj = null;
		int loc2 = buffer.readShort();
		int loc3 = 0;
		while(loc3 < loc2)
		{
			obj = new ObjectItem(buffer);
			this.inventory.add(obj);
			loc3++;
		}
		this.kamas = buffer.readVarInt();
		if(this.kamas < 0)
		{
			throw new Error("Forbidden value (" + this.kamas + ") on element of InventoryContentMessage.kamas.");
		}
	}

	private void deserializePresets(ByteArray buffer){
		Preset loc6 = null;
		IdolsPreset  loc7 = null;
		int loc2 = buffer.readShort();
		int loc3 = 0;
		while(loc3 < loc2)
		{
			loc6 = new Preset();
			loc6.deserialize(buffer);
			this.presets.add(loc6);
			loc3++;
		}
		int loc4 = buffer.readShort();
		int loc5 = 0;
		while(loc5 < loc4)
		{
			loc7 = new IdolsPreset();
			loc7.deserialize(buffer);
			this.idolsPresets.add(loc7);
			loc5++;
		}
	}
}