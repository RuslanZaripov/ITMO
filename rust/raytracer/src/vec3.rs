use std::ops::{Add, AddAssign, Div, Mul, Neg, Sub};
use crate::get_random_double;
use crate::utils::{get_random_double_in_range, sq};

#[derive(Debug, Copy, Clone)]
pub struct Vec3 {
    pub x: f64,
    pub y: f64,
    pub z: f64,
}

pub type Color = Vec3;
pub const BLACK: Color = Color { x: 0.0, y: 0.0, z: 0.0 };

// todo: change name to Vec3f64 or make generic
impl Vec3 {
    pub fn new(x: f64, y: f64, z: f64) -> Self {
        Self { x, y, z }
    }

    pub fn length(&self) -> f64 {
        (self.x * self.x + self.y * self.y + self.z * self.z).sqrt()
    }

    pub fn unit_vector(&self) -> Self {
        *self / self.length()
    }

    pub fn random() -> Self {
        Self::new(get_random_double(), get_random_double(), get_random_double())
    }

    pub fn random_in_range(min: f64, max: f64) -> Self {
        Self::new(
            get_random_double_in_range(min, max),
            get_random_double_in_range(min, max),
            get_random_double_in_range(min, max),
        )
    }

    pub fn near_zero(&self) -> bool {
        const S: f64 = 1e-8;
        self.x.abs() < S && self.y.abs() < S && self.z.abs() < S
    }
}

pub fn dot(left: &Vec3, right: &Vec3) -> f64 {
    return left.x * right.x + left.y * right.y + left.z * right.z
}

pub fn cross(left: &Vec3, right: &Vec3) -> Vec3 {
    Vec3::new(
        left.y * right.z - left.z * right.y,
        left.z * right.x - left.x * right.z,
        left.x * right.y - left.y * right.x,
    )
}

impl Add for Vec3 {
    type Output = Self;

    fn add(self, other: Self) -> Self::Output {
        Self::new(self.x + other.x, self.y + other.y, self.z + other.z)
    }
}

impl Sub for Vec3 {
    type Output = Self;

    fn sub(self, other: Self) -> Self::Output {
        Self::new(self.x - other.x, self.y - other.y, self.z - other.z)
    }
}

impl Mul for Vec3 {
    type Output = Self;

    fn mul(self, other: Self) -> Self::Output {
        Self::new(self.x * other.x, self.y * other.y, self.z * other.z)
    }
}

impl Mul<f64> for Vec3 {
    type Output = Self;

    fn mul(self, other: f64) -> Self::Output {
        Self::new(self.x * other, self.y * other, self.z * other)
    }
}

impl Mul<Vec3> for f64 {
    type Output = Vec3;

    fn mul(self, other: Vec3) -> Self::Output {
        Vec3::new(self * other.x, self * other.y, self * other.z)
    }
}

impl Div<f64> for Vec3 {
    type Output = Self;

    fn div(self, other: f64) -> Self::Output {
        Self::new(self.x / other, self.y / other, self.z / other)
    }
}

impl Neg for Vec3 {
    type Output = Self;

    fn neg(self) -> Self::Output {
        Self::new(-self.x, -self.y, -self.z)
    }
}

impl AddAssign for Vec3 {
    fn add_assign(&mut self, other: Self) {
        *self = Self::new(self.x + other.x, self.y + other.y, self.z + other.z);
    }
}

pub fn random_in_unit_sphere() -> Vec3 {
    loop {
        let p = Vec3::random_in_range(-1.0, 1.0);
        if p.length() < 1.0 {
            return p;
        }
    }
}

pub fn random_unit_vector() -> Vec3 {
    random_in_unit_sphere().unit_vector()
}

// todo: learn more about auto deref
pub fn reflect(v: Vec3, n: Vec3) -> Vec3 {
    v - 2.0 * dot(&v, &n) * n
}

pub fn refract(ray: Vec3, normal: Vec3, refractive_index_ratio: f64) -> Vec3 {
    let cos_theta = dot(&-ray, &normal).min(1.0);
    let r_perpendicular = refractive_index_ratio * (ray + cos_theta * normal);
    let r_parallel = -((1.0 - sq(r_perpendicular.length())).abs()).sqrt() * normal;
    r_perpendicular + r_parallel
}
