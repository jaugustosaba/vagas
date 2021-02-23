using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;


namespace Vagas.Models {

    [Table("distance")]
    public class Distance {
        [Column("from_location_id")]
        public long FromLocationId { get; set; }

        public Location FromLocation { get; set; }

        [Column("to_location_id")]
        public long ToLocationId { get; set; }

        public Location ToLocation { get; set; }

        [Column("value")]
        public int Value { get; set; }
    }

}