package roleplay.fight;

import utilities.ByteArray;

public class GameFightMinimalStatsPreparation extends GameFightMinimalStats {
    public int initiative = 0;
    
    public GameFightMinimalStatsPreparation()
    {
       super();
    }
    

    public void deserializeAs_GameFightMinimalStatsPreparation(ByteArray buffer)
    {
       super.deserialize(buffer);
       this.initiative = buffer.readVarInt();
       if(this.initiative < 0)
       {
          throw new Error("Forbidden value (" + this.initiative + ") on element of GameFightMinimalStatsPreparation.initiative.");
       }
    }
}
