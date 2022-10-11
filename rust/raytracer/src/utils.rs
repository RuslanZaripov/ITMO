use rand::Rng;

pub fn get_random_double_in_range(min: f64, max: f64) -> f64 {
    rand::thread_rng().gen_range(min..max)
}

pub fn get_random_double() -> f64 {
    rand::thread_rng().gen_range(0.0..1.0)
}