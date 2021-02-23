using System.Text.Json.Serialization;

namespace Vagas.Controller
{
    public class JobJson {
        [JsonPropertyName("empresa")]
        public string Company { get; set; }

        [JsonPropertyName("titulo")]
        public string Title { get; set; }

        [JsonPropertyName("descricao")]
        public string Description { get; set; }

        [JsonPropertyName("localizacao")]
        public string Location { get; set; }

        [JsonPropertyName("nivel")]
        public int Level { get; set; }
    }

}