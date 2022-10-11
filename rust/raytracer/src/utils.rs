use rand::Rng;

pub fn get_random_double_in_range(min: f64, max: f64) -> f64 {
    rand::thread_rng().gen_range(min..max)
}

pub fn get_random_double() -> f64 {
    rand::thread_rng().gen_range(0.0..1.0)
}

pub fn find_first(vec: Vec<f64>, lambda: impl FnMut(&f64) -> bool) -> Option<f64> {
    vec.into_iter()
        .filter(lambda)
        .nth(0)
}

pub fn sq(x: f64) -> f64 {
    f64::powf(x, 2.0)
}