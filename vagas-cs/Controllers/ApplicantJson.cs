using System.Text.Json.Serialization;

namespace Vagas.Controller
{
    public class ApplicantJson {
        [JsonPropertyName("nome")]
        public string Name { get; set; }

        [JsonPropertyName("profissao")]
        public string profession { get; set; }

        [JsonPropertyName("localizacao")]
        public string Location { get; set; }

        [JsonPropertyName("nivel")]
        public int Level { get; set; }
    }

}