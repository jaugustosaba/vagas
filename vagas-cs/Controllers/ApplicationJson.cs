using System.Text.Json.Serialization;

namespace Vagas.Controller
{
    public class ApplicationJson {
        [JsonPropertyName("id_vaga")]
        public long JobId { get; set; }

        [JsonPropertyName("id_pessoa")]
        public long ApplicantId { get; set; }
    }

}