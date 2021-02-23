using System.Text.Json.Serialization;

namespace Vagas.Controller
{
    public class ScoreJson {

        [JsonPropertyName("nome")]
        public string Name { get; set; }

        [JsonPropertyName("profissao")]
        public string Profession { get; set; }

        [JsonPropertyName("localizacao")]
        public string Localization { get; set; }

        [JsonPropertyName("nivel")]
        public long Level { get; set; }

        [JsonPropertyName("score")]
        public long score { get; set; }

    }
}